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
        # Partial match for "Vantagem de Classe" e.g. "Clérigo (Vantagem de Classe)" -> "Clérigo"
        if '(' in n_text:
             base = n_text.split('(')[0].strip()
             if base == normalize(ref):
                 return True
    return False

# --- LISTS FROM PROMPT ---

ANCESTRALIDADES_FANTASIA = [
    "Celestial", "Centauro", "Deva", "Doppleganger", "Draconato", "Dragonfolk", "Draconiano", # Mapping Draconato/Draconiano
    "Elemental", "Meio-Gigante", "Gnomo", "Goblin", "Golem",
    "Infernato", "Infernal", "Insetoide", "Kenku", "Birdfolk",
    "Morto-Vivo", "Renascido", # Renascido is likely the translation for Dhampir/Undead-ish in some PT translations or "Morto-Vivo (Personagem)"
    "Minotauro", "Ogro", "Orc",
    "Anão", "Anões", "Elfo", "Elfos", "Elfo Sombrio", "Anão das Profundezas" # Variants
]

COMPLICACOES_FANTASIA = [
    "Alvo de Caça", "Código de Honra de Cavalaria", "Compulsão Alquímica",
    "Dependência Mágica", "Desonrado", "Estigma Social", "Monstruoso",
    "Fraqueza Elemental", "Ganância por Ouro", "Juramento de Sangue",
    "Maldito", "Marca do Caos", "Medo de Magia"
]

VANTAGENS_FANTASIA = [
    # Combate
    "Aljava Cheia", "Arqueiro Arcano", "Assalto Impetuoso", "Carga de Cavalaria", "Carga", # Mapping Carga
    "Combatente de Escudo", "Parede de Escudos", # Mapping? Or strict? "Combatente de Escudo" might be "Shield fighter" or similar.
    "Defesa com Duas Armas", "Especialista em Arma", "Mestre de Armas",
    "Matador de Monstros", "Táticas de Alcance", "Combate Próximo", # "Close Fighting" -> Combate Próximo in PT translation usually.

    # Profissionais (Class Edges)
    "Alquimista", "Bardo", "Cavaleiro", "Clérigo", "Druida", "Inquisidor",
    "Ladino", "Mago", "Monge", "Necromante", "Paladino", "Patrulheiro", "Ranger", "Swashbuckler",
    "Antecedente Arcano (Alquimista)", "Antecedente Arcano (Bardo)", "Antecedente Arcano (Clérigo)",
    "Antecedente Arcano (Druida)", "Antecedente Arcano (Feiticeiro)", "Antecedente Arcano (Mago)", "Antecedente Arcano (Necromante)",
    "Antecedente Arcano (Paladino)", "Antecedente Arcano (Ranger)", "Antecedente Arcano (Xamã)", "Antecedente Arcano (Invocador)", "Antecedente Arcano (Ilusionista)", "Antecedente Arcano (Elementalista)",

    # Mágicas
    "Alquimia Natural", "Conjurador de Batalha", "Mago de Batalha", # Mapping
    "Familiar", "Mestre dos Elementos", "Poder Reserva",
    "Recarga de Pontos de Poder", "Ritualista",

    # Sociais
    "Herdeiro de Título", "Nobreza de Sangue", "Rico"
]

# Note: "Rico" is also in Core. User says "Rico (versão Fantasia...)".
# Strategy: If it is in 'suplementos_vantagens.json', it is the non-core one (or a duplicate).
# If I move it to 'fantasia_', I should rename or ensure it doesn't conflict if names are identical keys.
# But 'DataLoader' merges lists. Duplicate IDs are bad. Duplicate Names are confusing.
# If IDs are distinct (e.g. 'rico_fantasy'), good. If IDs are 'rico', bad.
# I will inspect the data.

EQUIPAMENTOS_FANTASIA = [
    # Aventura
    "Alforjes", "Ampulheta", "Arpéu de Metal", "Arpéu", "Arca de Tesouro", "Baú",
    "Bolsa de Alquimista", "Caltrops", "Estrepes", "Espinhos", "Pitões",
    "Estojo de Mapas", "Ferramentas de Ladrão", "Giz", "Grimório",
    "Instrumento Musical", "Kit de Curandeiro", "Luneta de Bronze", "Luneta",
    "Pedra de Amolar", "Símbolo Sagrado", "Tenda de Campanha", "Tenda",

    # Alquimicos
    "Ácido", "Frasco de Ácido", "Água Benta", "Antitoxina", "Bastão de Fumaça",
    "Bastão Solar", "Bolsa Enredapé", "Tanglefoot Bag", "Fogo Alquímico",
    "Óleo Inflamável", "Óleo", "Pedra Trovão", "Poção", "Tocha Eterna",

    # Armaduras/Escudos
    "Armadura de Placas Completa", "Placas", "Field Plate",
    "Armadura de Escamas", "Scale Mail",
    "Armadura de Anéis", "Ring Mail", "Cota de Malha", # Sometimes Ring Mail maps to Cota de Malha in simplified lists, but usually distinct.
    "Couro Batido", "Studded Leather",
    "Barda", "Broquel", "Escudo de Pavês", "Pavês", "Elmo Fechado"
]

ARMAS_FANTASIA = [
    "Alabarda", "Alfange", "Adaga de Aparar", "Bisarma", "Chakram",
    "Chicote de Guerra", "Cimitarra", "Corrente com Espinhos",
    "Espada Bastarda", "Espada Gancho", "Flamberge", "Foice de Guerra",
    "Gadanha", "Glaive", "Katana", "Maça Estrela", "Morningstar",
    "Malho de Guerra", "Mangual Pesado", "Martelo Meteoro", "Naginata",
    "Podão de Guerra", "Tridente",

    "Arco Composto", "Arco Curto", "Arco Longo", "Besta de Mão",
    "Besta de Repetição", "Besta Leve", "Besta Pesada", "Boleadeira",
    "Shuriken", "Zarabatana"
]

MATERIAIS_FANTASIA = [
    "Adamante", "Adamantina", "Mithral", "Ferro Frio", "Prata Alquímica",
    "Madeira Negra", "Darkwood", "Couro de Dragão"
]

VEICULOS_FANTASIA = [
    "Aríete", "Balista", "Catapulta", "Trabuco", "Torre de Cerco",
    "Galé", "Galeão", "Barco de Guerra", "Carruagem de Guerra", "Carruagem", "Trenó de Neve", "Trenó"
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

def should_move_ancestralidade(item):
    name = item.get('nome', '')
    origem = item.get('origem', '').upper()

    if origem == 'FANTASIA' or origem == 'FANTASIABUSCATRILHA':
        return True

    if matches_any(name, ANCESTRALIDADES_FANTASIA):
        return True

    return False

def should_move_complicacao(item):
    name = item.get('name', '') # Complicacao uses 'name'
    origem = item.get('origem', '').upper()

    if origem == 'FANTASIA' or origem == 'FANTASIABUSCATRILHA':
        return True

    if matches_any(name, COMPLICACOES_FANTASIA):
        return True

    return False

def should_move_vantagem(item):
    name = item.get('nome', '')
    origem = item.get('origem', '').upper()

    if origem == 'FANTASIA' or origem == 'FANTASIABUSCATRILHA':
        return True

    if matches_any(name, VANTAGENS_FANTASIA):
        return True

    # Special check for Class Edges or Arcane Backgrounds
    if "ANTECEDENTE ARCANO" in name.upper() and ("CLÉRIGO" in name.upper() or "DRUIDA" in name.upper() or "MAGO" in name.upper()):
        return True

    return False

def should_move_equipamento(item, category_origem):
    name = item.get('nome', '')
    # Item origin overrides category origin if present
    origem = item.get('origem', category_origem).upper()

    if origem == 'FANTASIA' or origem == 'FANTASIABUSCATRILHA':
        return True

    if matches_any(name, EQUIPAMENTOS_FANTASIA + ARMAS_FANTASIA + MATERIAIS_FANTASIA + VEICULOS_FANTASIA):
        return True

    return False

def process_file(source_path, dest_path, check_func, is_equip=False):
    data = load_json(source_path)
    if not data:
        print(f"Skipping {source_path}, empty or not found.")
        return

    kept_items = []
    moved_items = []

    if is_equip:
        # Equipamento structure is List<Category>
        for cat in data:
            cat_origem = cat.get('origem', 'SUPLEMENTO')

            # If category itself is explicitly Fantasy (e.g. from previous manual edits), move whole category?
            # Or scan items. Let's scan items to be precise as requested.

            cat_moved_items = []
            cat_kept_items = []

            for item in cat.get('itens', []):
                if should_move_equipamento(item, cat_origem):
                    item['origem'] = 'FANTASIA'
                    cat_moved_items.append(item)
                else:
                    cat_kept_items.append(item)

            if cat_moved_items:
                # Create a category for moved items
                new_cat = cat.copy()
                new_cat['itens'] = cat_moved_items
                new_cat['origem'] = 'FANTASIA'
                moved_items.append(new_cat)

            if cat_kept_items:
                cat['itens'] = cat_kept_items
                kept_items.append(cat)

    else:
        # Simple List
        is_wrapped = isinstance(data, dict) and 'pericias' in data # Special handling for pericias if needed, but likely not used here
        items = data['pericias'] if is_wrapped else data

        for item in items:
            if check_func(item):
                item['origem'] = 'FANTASIA'
                moved_items.append(item)
            else:
                kept_items.append(item)

        if is_wrapped:
            kept_items = {'pericias': kept_items}
            # Moved items structure for pericias would be dict too, but we are doing vant/comp/anc mostly.
            # Assuming lists for target files.

    if moved_items:
        save_json(moved_items, dest_path)
        print(f"Moved {len(moved_items)} items to {dest_path}")

    save_json(kept_items, source_path)
    print(f"Updated {source_path} with remaining items.")

def main():
    # Ancestralidades
    process_file(
        'app/src/main/assets/suplementos_listaancestralidade.json',
        'app/src/main/assets/fantasia_listaancestralidade.json',
        should_move_ancestralidade
    )

    # Complicacoes
    process_file(
        'app/src/main/assets/suplementos_complicacoes.json',
        'app/src/main/assets/fantasia_complicacoes.json',
        should_move_complicacao
    )

    # Vantagens
    process_file(
        'app/src/main/assets/suplementos_vantagens.json',
        'app/src/main/assets/fantasia_vantagens.json',
        should_move_vantagem
    )

    # Equipamentos
    process_file(
        'app/src/main/assets/suplementos_equipamentos.json',
        'app/src/main/assets/fantasia_equipamentos.json',
        should_move_equipamento,
        is_equip=True
    )

if __name__ == "__main__":
    main()
