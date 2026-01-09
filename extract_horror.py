import json
import os
import unicodedata

def normalize(text):
    if not text: return ""
    return unicodedata.normalize('NFKD', text).encode('ASCII', 'ignore').decode('utf-8').lower().strip()

def matches_any(text, reference_list):
    n_text = normalize(text)
    for ref in reference_list:
        if normalize(ref) == n_text:
            return True
        # Partial match
        if '(' in n_text:
             base = n_text.split('(')[0].strip()
             if base == normalize(ref):
                 return True
    return False

# --- LISTS FROM PROMPT ---

ANCESTRALIDADES_HORROR = [
    "Anjo", "Dhampir", "Meio-Vampiro", "Fantasma", "Lobisomem", "Múmia",
    "Remanescente", "Retornado", "Zumbi", "Vampiro", "Boneco Vivo", "Ventríloquo",
    "Carniçal", "Ghoul"
]

COMPLICACOES_HORROR = [
    "Abalado pelo Medo", "Assombrado", "Dependência Sangue", "Dependência Carne", "Dependência", # Generic 'Dependencia' might be Core, check description or origin
    "Desequilibrado", "Fobia", "Insanidade", "Marca da Besta", "Pesadelos",
    "Possuído", "Sonâmbulo", "Toque da Morte", "Transformação Incontrolável",
    "Traumatizado"
]

VANTAGENS_HORROR = [
    # Combate
    "Assassino de Monstros", "Atirador de Elite", "Combatente Implacável",
    "Coragem Inabalável", "Destruidor de Mortos-Vivos", "Exorcista",
    "Golpe de Misericórdia", "Resiliência Mental",

    # Sociais/Gerais
    "Investigador Paranormal", "Médium", "Ocultista Profissional",
    "Resistência ao Medo", "Sexto Sentido", "Sensitivo", "Vontade de Viver",

    # Poderes
    "Antecedente Arcano (Rituais de Sangue)", "Magia Negra",
    "Necromancia", "Invocador de Entidades",

    # Existing Horror items might have specific IDs like 'conjurador_silencioso_horror'
]

EQUIPAMENTOS_HORROR = [
    # Investigação
    "Gravador de Voz", "EVP", "Câmera Infravermelha", "Detector de EMF",
    "Kit de Exorcismo", "Kit de Taxidermia", "Lanterna de Luz Negra", "UV",
    "Sal Sagrado", "Soro da Verdade", "Vidro de Água Benta", "Spray de Água Benta",
    "Estacas de Madeira", "Estaca", "Maleta Médica Vitoriana",

    # Proteção
    "Amuleto de Proteção", "Alho", "Círculo de Sal", "Cruz", "Crucifixo", "Símbolo de Fé",
    "Maleta de Caçador de Monstros",

    # Armas
    "Besta de Repetição", "Lança-Chamas Portátil", "Maça com Espinhos de Prata",
    "Martelo e Estaca", "Rifle de Precisão",

    # Munição
    "Bala de Prata", "Balas de Prata", "Bala de Madeira", "Balas de Madeira",
    "Bala com Água Benta", "Balas com Água Benta",
    "Bala de Ferro Frio", "Balas de Ferro Frio",
    "Cartucho de Sal", "Cartuchos de Sal",
    "Munição Incendiária", "Fósforo Branco"
]

VEICULOS_HORROR = [
    "Coche funerário", "Furgão de Investigação Paranormal", "Furgão",
    "Laboratório Móvel", "Ambulância de Isolamento", "Ambulância"
]

def load_json(path):
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        return []

def save_json(data, path):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def should_move(item, ref_list, explicit_origin_check=True):
    name = item.get('nome', item.get('name', '')) # Support 'nome' or 'name'
    origem = item.get('origem', '').upper()

    if explicit_origin_check and (origem == 'HORROR' or origem == 'HORROR_COMPANION'):
        return True

    if matches_any(name, ref_list):
        return True

    return False

def process_file(source_path, dest_path, ref_list, is_equip=False):
    data = load_json(source_path)
    if not data:
        print(f"Skipping {source_path}, empty or not found.")
        return

    kept_items = []
    moved_items = []

    if is_equip:
        for cat in data:
            cat_origem = cat.get('origem', 'SUPLEMENTO')
            cat_moved_items = []
            cat_kept_items = []

            for item in cat.get('itens', []):
                # Check origin on item, or fallback to category, or match name
                item_origem = item.get('origem', cat_origem).upper()
                if (item_origem == 'HORROR' or item_origem == 'HORROR_COMPANION') or matches_any(item.get('nome',''), ref_list):
                    item['origem'] = 'HORROR'
                    cat_moved_items.append(item)
                else:
                    cat_kept_items.append(item)

            if cat_moved_items:
                new_cat = cat.copy()
                new_cat['itens'] = cat_moved_items
                new_cat['origem'] = 'HORROR'
                moved_items.append(new_cat)

            if cat_kept_items:
                cat['itens'] = cat_kept_items
                kept_items.append(cat)
    else:
        is_wrapped = isinstance(data, dict) and 'pericias' in data
        items = data['pericias'] if is_wrapped else data

        for item in items:
            if should_move(item, ref_list):
                item['origem'] = 'HORROR'
                moved_items.append(item)
            else:
                kept_items.append(item)

        if is_wrapped:
            kept_items = {'pericias': kept_items}

    if moved_items:
        save_json(moved_items, dest_path)
        print(f"Moved {len(moved_items)} items to {dest_path}")

    save_json(kept_items, source_path)
    print(f"Updated {source_path} with remaining items.")

def main():
    # Ancestralidades
    process_file(
        'app/src/main/assets/suplementos_listaancestralidade.json',
        'app/src/main/assets/horror_listaancestralidade.json',
        ANCESTRALIDADES_HORROR
    )

    # Complicacoes
    process_file(
        'app/src/main/assets/suplementos_complicacoes.json',
        'app/src/main/assets/horror_complicacoes.json',
        COMPLICACOES_HORROR
    )

    # Vantagens
    process_file(
        'app/src/main/assets/suplementos_vantagens.json',
        'app/src/main/assets/horror_vantagens.json',
        VANTAGENS_HORROR
    )

    # Equipamentos
    process_file(
        'app/src/main/assets/suplementos_equipamentos.json',
        'app/src/main/assets/horror_equipamentos.json',
        EQUIPAMENTOS_HORROR + VEICULOS_HORROR,
        is_equip=True
    )

if __name__ == "__main__":
    main()
