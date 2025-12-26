import json

filepath = 'app/src/main/assets/poderes.json'

with open(filepath, 'r') as f:
    powers = json.load(f)

existing_ids = {p['id'] for p in powers}

# Map new_id -> existing_source_id (or None for brand new)
mappings = {
    "amigo_feras": "amigo_das_feras",
    "andar_paredes": "andar_nas_paredes",
    "dano_campo": "campo_de_dano",
    "convocar_aliado": "conjurar_aliado",
    "guerreiro": "dadiva_do_guerreiro",
    "marionete": "fantoche",
    "luz_escuridao": "iluminar_obscurecer",
    "leitura_mentes": "leitura_mental",
    "manipular_elementos": "manipulacao_elemental",
    "velocidade": "morosidade_velocidade",
    "rapidez": "morosidade_velocidade",
    "visao_no_escuro": "visao_sombria",
    "metamorfose": "mudanca_de_forma",
    # New Generic ones (None source)
    "consagrar": None,
    "simbolo_sagrado": None,
    "busca_visao": None,
    "caminhar_ermos": None,
    "bugigangas": None,
    "jogatina": None,
    "bolso_dimensional": None,
    "som": None, # som_silencio exists, maybe map to that? Prompt says "som".
    "peregrino": None,
    "maldicao": None,
    "santuario": None,
    "invocar_demonio": None,
    "animacao": None,
    "cova": None,
    "enterrar": None,
    "escuridao": None,
    "entidade": None,
    "fogo_infernal": None,
    "vendaval": None,
    "mirar": None,
    "alivio": None,
    "amortecer": None,
    "furia": None
}

# Special check for "som" -> "som_silencio"
# If I map "som" to "som_silencio", it's safer.
mappings["som"] = "som_silencio"

new_entries = []

for new_id, source_id in mappings.items():
    if new_id in existing_ids:
        print(f"Skipping {new_id}, already exists.")
        continue

    if source_id and source_id in existing_ids:
        # Clone
        source_power = next(p for p in powers if p['id'] == source_id)
        new_power = source_power.copy()
        new_power['id'] = new_id
        # Optional: Append tag to indicate alias/variant if needed, or leave as is
        new_entries.append(new_power)
        print(f"Created alias {new_id} from {source_id}")
    else:
        # Create Generic
        new_power = {
            "id": new_id,
            "origem": "BASICO", # Defaulting to Basico to ensure visibility if logic relies on it, or could use "EXTRA"
            "nome": new_id.replace("_", " ").upper(),
            "estagio": "Novato",
            "pontosDePoder": 0,
            "distancia": "TBD",
            "duracao": "TBD",
            "manifestacoes": [],
            "descricao": "Descrição pendente para " + new_id,
            "modificadores": [],
            "tags": ["novo"]
        }
        new_entries.append(new_power)
        print(f"Created new generic power {new_id}")

powers.extend(new_entries)

# Sort by ID for tidiness
powers.sort(key=lambda x: x['id'])

with open(filepath, 'w') as f:
    json.dump(powers, f, indent=2, ensure_ascii=False)

print("Done updating poderes.json")
