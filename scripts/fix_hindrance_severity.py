import json
import re
import unicodedata

def normalize_key(s):
    nfkd_form = unicodedata.normalize('NFKD', s)
    only_ascii = nfkd_form.encode('ASCII', 'ignore').decode('utf-8')
    return re.sub(r'[^A-Z0-9]', '', only_ascii.upper())

STANDARD_SEVERITY = {
    "NAOSABENADAR": "Menor",
    "SANGUINARIO": "Maior",
    "CURIOSO": "Maior",
    "DESASTRADO": "Menor",
    "FORASTEIRO": "Menor",
    "PACIFISTA": "Menor",
    "VOTO": "Menor",
    "SEMESCRUPULOS": "Menor",
    "INIMIGO": "Menor",
    "INIMIGORACIAL": "Menor",
    "INIMIGOANCESTRAL": "Menor",
    "PROCURADO": "Menor",
    "POBREZA": "Menor",
    "ANALFABETO": "Menor",
    "CEGO": "Maior",
    "SURDO": "Menor",
    "MUDO": "Maior",
    "UMBRACOSO": "Maior",
    "UMAPERNASO": "Maior",
    "OBESO": "Menor",
    "ANEMICO": "Menor",
    "PECULIARIDADE": "Menor",
    "DESAGRADAVEL": "Menor",
    "RUDE": "Menor",
    "SENSIVEL": "Maior", # Thin Skinned
    "CIBERRESISTENCIA": "Menor",
    "CIRCUITOSDEASIMOV": "Maior", # Pacifista Maior implied
    "PROGRAMADO": "Maior", # Voto Maior implied
    "TREINADOSPARAGUERRA": "Menor", # Clueless?
    "OBVIO": "Menor",
    "TRANSTORNODESEPARACAO": "Maior", # Usually significant
    "INCAPAZDEFALAR": "Maior", # Mute

    # Fantasy Additions
    "ARROGANTE": "Maior",
    "IMPULSIVO": "Menor",
    "BOCAGRANDE": "Menor",
    "COVARDE": "Maior", # Yellow
    "GANANCIOSO": "Menor"
}

def process_file(filepath):
    print(f"Fixing severity in {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for entry in data:
        for hab in entry.get("habilidades", []):
            hid = normalize_key(hab.get("id", ""))

            # Correct ID if needed (TAMANHO_2 -> TAMANHO_MAIS_2)
            if hid == "TAMANHO2": # Normalized ID for "TAMANHO_2" or "Tamanho +2"
                 # Check if description or name implies +2
                 if "+2" in hab["nome"] or "+2" in hab["descricao"]:
                     hab["id"] = "TAMANHO_MAIS_2"
                     hid = "TAMANHOMAIS2"

            name = hab["nome"]
            cat = hab.get("category", "")

            # Identify if it is a hindrance
            # Re-check traits marked as negative traits to see if they are actually hindrances
            is_hindrance = cat == "racial_hindrance" or hid in STANDARD_SEVERITY

            if is_hindrance:
                hab["category"] = "racial_hindrance"

                # Determine Severity
                severity = None

                # 1. Explicit in Name
                if "(MAIOR)" in name.upper() or " MAIOR" in name.upper():
                    severity = "Maior"
                elif "(MENOR)" in name.upper() or " MENOR" in name.upper():
                    severity = "Menor"

                # 2. Default map
                if not severity:
                    severity = STANDARD_SEVERITY.get(hid, "Menor")

                hab["severity"] = severity

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

process_file("app/src/main/assets/basico_ancestralidades.json")
process_file("app/src/main/assets/fantasia_ancestralidades.json")
process_file("app/src/main/assets/scifi_ancestralidades.json")
