import json
import re
import unicodedata

def normalize_string(s):
    # Remove accents
    nfkd_form = unicodedata.normalize('NFKD', s)
    only_ascii = nfkd_form.encode('ASCII', 'ignore').decode('utf-8')
    # Uppercase
    return only_ascii.upper()

def get_root_name(s):
    # Split by ( to remove modifiers like (Maior)
    return s.split('(')[0].strip()

def generate_id(s):
    root = get_root_name(s)
    norm = normalize_string(root)
    # Replace non-alphanumeric with _
    clean = re.sub(r'[^A-Z0-9]+', '_', norm).strip('_')
    return clean

STANDARD_HINDRANCES = {
    "FORASTEIRO", "PACIFISTA", "VOTO", "DESASTRADO", "INIMIGO", "PECULIARIDADE",
    "ANEMICO", "PROCURADO", "POBREZA", "ANALFABETO", "CEGO", "SURDO", "MUDO",
    "UM_BRACO_SO", "UMA_PERNA_SO", "OBESO", "MIOPE", "SANGUINARIO", "INIMIGO_RACIAL",
    "DESAGRADAVEL", "BAIXA_TECNOLOGIA", "SEM_ESCRUPULOS", "PROGRAMADO",
    "CURIOSO", "SENSIVEL", "RUDE", "CIBER_RESISTENCIA", "CIRCUITOS_DE_ASIMOV",
    "TREINADOS_PARA_A_GUERRA", "OBVIO", "TRANSTORNO_DE_SEPARACAO", "INCAPAZ_DE_FALAR"
}

STANDARD_EDGES = {
    "PRONTIDAO", "SORTE", "CAMPEAO", "ADAPTAVEL", "AMBIDESTRO", "CORAJOSO",
    "SEM_MEDO", "ALERTA", "ARISTOCRATA", "ATRAENTE", "LINHAGEM_NOBRE"
}

def process_file(filepath):
    print(f"Processing {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for entry in data:
        existing_ids = set()
        new_habilidades = []

        # 1. Process existing habilidades (update categories, standardize IDs)
        for hab in entry.get("habilidades", []):
            name = hab["nome"]
            # Generate stable ID if missing or weak
            current_id = hab.get("id")
            generated_id = generate_id(name)

            # Prefer manually set ID if it looks valid (uppercase, no weird chars), else generate
            final_id = generated_id
            if current_id and re.match(r'^[A-Z0-9_]+$', current_id):
                final_id = current_id

            # Fix IDs that were bad from previous run (e.g. ADAPTVEL)
            # Actually, let's force regeneration for known issues or just trust the generator
            # The generator is robust now.
            final_id = generated_id

            hab["id"] = final_id

            # Determine Category
            cat = hab.get("category", "")

            # Detect based on ID
            if final_id in STANDARD_HINDRANCES:
                cat = "racial_hindrance"
            elif final_id in STANDARD_EDGES:
                cat = "racial_edge"

            # Heuristics for text
            if "ganha a Vantagem" in hab["descricao"] or "começam com qualquer Vantagem" in hab["descricao"]:
                # Unless it's just 'Sua Vantagem...' describing a trait.
                # Usually "Ganha a Vantagem X" means it is an Edge wrapper.
                # But if we identified the ID as an Edge, we are good.
                pass

            # Update category
            hab["category"] = cat

            if final_id not in existing_ids:
                new_habilidades.append(hab)
                existing_ids.add(final_id)
            else:
                # Merge? Or skip duplicate?
                # If we have a duplicate, it might be the script running twice.
                # We keep the first one usually.
                pass

        # 2. Process desvantagens (legacy list)
        for desv in entry.get("desvantagens", []):
            gen_id = generate_id(desv)

            if gen_id in existing_ids:
                # Already exists in abilities (e.g. Android Forasteiro), check if category is correct there
                # We can update the existing entry if it was marked as a trait instead of hindrance
                for h in new_habilidades:
                    if h["id"] == gen_id:
                        if gen_id in STANDARD_HINDRANCES:
                            h["category"] = "racial_hindrance"
                        elif h["category"] not in ["racial_hindrance", "racial_edge"]:
                             h["category"] = "racial_trait_negative" # Default for desvantagens list items
                continue

            # Create new
            cat = "racial_trait_negative"
            if gen_id in STANDARD_HINDRANCES:
                cat = "racial_hindrance"

            new_habilidades.append({
                "nome": desv,
                "descricao": desv, # Placeholder
                "id": gen_id,
                "category": cat
            })
            existing_ids.add(gen_id)

        # 3. Process vantagensGratis (legacy list)
        for vant in entry.get("vantagensGratis", []):
            gen_id = generate_id(vant)

            if gen_id in existing_ids:
                for h in new_habilidades:
                    if h["id"] == gen_id:
                        h["category"] = "racial_edge"
                continue

            new_habilidades.append({
                "nome": vant,
                "descricao": vant,
                "id": gen_id,
                "category": "racial_edge"
            })
            existing_ids.add(gen_id)

        entry["habilidades"] = new_habilidades
        entry["desvantagens"] = []
        entry["vantagensGratis"] = []

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

process_file("app/src/main/assets/basico_ancestralidades.json")
process_file("app/src/main/assets/fantasia_ancestralidades.json")
process_file("app/src/main/assets/scifi_ancestralidades.json")
