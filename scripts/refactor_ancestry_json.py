import json
import re

def parse_hindrance_level(name):
    if "(Maior)" in name or "Maior" in name:
        return "Major"
    if "(Menor)" in name or "Menor" in name:
        return "Minor"
    return "Minor" # Default or handle as "trait"

def normalize_key(s):
    return re.sub(r'[^A-Z0-9]', '', s.upper())

STANDARD_HINDRANCES = {
    "FORASTEIRO": "Forasteiro",
    "PACIFISTA": "Pacifista",
    "VOTO": "Voto",
    "DESASTRADO": "Desastrado",
    "INIMIGO": "Inimigo",
    "PECULIARIDADE": "Peculiaridade",
    "ANEMICO": "Anêmico",
    "PROCURADO": "Procurado",
    "POBREZA": "Pobreza",
    "ANALFABETO": "Analfabeto",
    "CEGO": "Cego",
    "SURDO": "Surdo",
    "MUDO": "Mudo",
    "UMBRACOSO": "Um Braço Só",
    "UMAPERNASO": "Uma Perna Só",
    "OBESO": "Obeso",
    "MIUPE": "Míope",
    "SANGUINARIO": "Sanguinário",
    "INIMIGORACIAL": "Inimigo Racial",
    "DESAGRADAVEL": "Desagradável",
    "BAIXATECNOLOGIA": "Baixa Tecnologia",
    "SEMESCRUPULOS": "Sem Escrúpulos",
    "CIRCUITOSDEASIMOV": "Circuitos de Asimov", # Essentially Pacifist
    "PROGRAMADO": "Programado", # Vow
    "TREINADOSPARAGUERRA": "Treinados para a Guerra", # Lack of Knowledge
    "CURIOSO": "Curioso",
    "SENSIVEL": "Sensível",
    "RUDE": "Rude",
    "OBVIO": "Óbvio",
    "APARARBAIXO": "Aparar Baixo", # Trait? Usually a trait penalty to Parry.
    "FRACO": "Fraco", # Trait (-1 Str)
    "LENTO": "Lento", # Trait (Pace)
    "NAOPODECURAR": "Não Pode Curar", # Trait
    "TRANSTORNODESEPARACAO": "Transtorno de Separação", # Trait? Usually specific hindrance-like but unique.
    "INCAPAZDEFALAR": "Incapaz de Falar", # Trait
    "CIBERRESISTENCIA": "Ciber-Resistência", # Specific hindrance? Or trait? Book says "Hindrance (p. 129)".
    "FRAQUEZAAMBIENTAL": "Fraqueza Ambiental", # Trait.
    "DEPENDENCA": "Dependência", # Trait.
    "MOVIMENTACAOREDUZIDA": "Movimentação Reduzida", # Trait.
    "NAOSABENADAR": "Não Sabe Nadar", # Trait.
    "TAMANHOMENOS1": "Tamanho -1", # Trait.
    "GRANDE": "Grande", # Trait? Usually Size +2, but "Grande" hindrance exists? No, usually "Grande" is Size. "Obeso" is hindrance.
    "FRAGIL": "Frágil" # Trait (-1 Toughness).
}

# Explicit mapping for "Standard Hindrances" vs "Racial Traits (Negative)"
ACTUAL_HINDRANCES = {
    "FORASTEIRO", "PACIFISTA", "VOTO", "DESASTRADO", "INIMIGO", "PECULIARIDADE",
    "ANEMICO", "PROCURADO", "POBREZA", "ANALFABETO", "CEGO", "SURDO", "MUDO",
    "UMBRACOSO", "UMAPERNASO", "OBESO", "MIUPE", "SANGUINARIO", "INIMIGORACIAL",
    "DESAGRADAVEL", "BAIXATECNOLOGIA", "SEMESCRUPULOS", "PROGRAMADO",
    "CURIOSO", "SENSIVEL", "RUDE", "CIBERRESISTENCIA", "CIRCUITOSDEASIMOV",
    "TREINADOSPARAGUERRA", "OBVIO", "TRANSTORNODESEPARACAO", "INCAPAZDEFALAR"
}

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for entry in data:
        new_habilidades = []

        # Process existing habilidades
        for hab in entry.get("habilidades", []):
            cat = hab.get("category", "")
            name_key = normalize_key(hab["nome"])

            # Reclassify based on ID or Name if generic trait category
            if cat in ["racial_trait_positive", "racial_trait_negative", "racial_trait_neutral", None]:
                if name_key in ACTUAL_HINDRANCES:
                    cat = "racial_hindrance"
                elif "VANTAGEM" in name_key or "EDGE" in name_key:
                    cat = "racial_edge"
                elif "SENTIDOSAGUCADOS" in name_key: # Usually Alertness Edge or d6 Notice
                    # Text check
                    if "Vantagem Prontidão" in hab["descricao"]:
                        cat = "racial_edge"
                    else:
                        cat = "racial_trait_positive"
                elif "PRONTIDAO" in name_key:
                     cat = "racial_edge"
                elif "SORTE" in name_key:
                     cat = "racial_edge"
                elif "CAMPEAO" in name_key:
                     cat = "racial_edge"
                elif "ADAPTAVEL" in name_key:
                     cat = "racial_edge"
                elif "AMBIDESTRO" in name_key:
                     cat = "racial_edge"

            hab["category"] = cat
            new_habilidades.append(hab)

        # Process desvantagens list (Strings)
        for desv in entry.get("desvantagens", []):
            name_key = normalize_key(desv.split("(")[0])
            is_major = "(Maior)" in desv or "(Major)" in desv

            cat = "racial_trait_negative"
            if name_key in ACTUAL_HINDRANCES:
                cat = "racial_hindrance"

            # Check if already in habilidades to avoid dups
            if not any(h["nome"] == desv for h in new_habilidades):
                new_habilidades.append({
                    "nome": desv,
                    "descricao": desv, # Placeholder description, usually fetched from compendium in app logic if missing
                    "id": name_key,
                    "category": cat
                })

        # Process vantagensGratis list (Strings)
        for vant in entry.get("vantagensGratis", []):
            name_key = normalize_key(vant.split("(")[0])
            cat = "racial_edge"

            if not any(h["nome"] == vant for h in new_habilidades):
                new_habilidades.append({
                    "nome": vant,
                    "descricao": vant, # Placeholder
                    "id": name_key,
                    "category": cat
                })

        entry["habilidades"] = new_habilidades
        entry["desvantagens"] = []
        entry["vantagensGratis"] = []

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

process_file("app/src/main/assets/basico_ancestralidades.json")
process_file("app/src/main/assets/fantasia_ancestralidades.json")
process_file("app/src/main/assets/scifi_ancestralidades.json")
