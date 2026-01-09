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

ANCESTRALIDADES_SCIFI = [
    "Androide", "Robô", "Bio-Orgânico", "Construído", "Floriano", "Híbrido Genético",
    "Insetoide Galáctico", "Insectoide", "Pequeno Cinzento", "Grey", "Ser de Energia",
    "Ciborgue", "Povo-Búfalo", "Aurax", "Povo-Alado", "Avion", "Kalian", "Povo de Quatro Braços",
    "Felinídeo", "Rakashan", "Sauriano", "Yeti", "Floran" # Expanding list based on known Sci-Fi items
]

COMPLICACOES_SCIFI = [
    "Dependência de Recarga", "Cyberpsicose", "Falha de Software", "Rastreável",
    "Programado", "Vulnerabilidade a PEM", "Atmosfera Específica", "Ciclo de Combustível",
    "Enjoo de Gravidade Zero", "Dependência Tecnológica", "Procurado (Império/Corporação)"
]

VANTAGENS_SCIFI = [
    # Profissionais
    "Engenheiro de Sistemas", "Hacker de Combate", "Piloto Estelar",
    "Especialista em Cyberware", "Mecânico de Robôs", "Ás (Piloto)", "Engenheiro Espacial", "Jockey de Mecha",

    # Combate
    "Atirador de Precisão Digital", "Combatente em Gravidade Zero",
    "Mestre de Armas de Energia", "Operador de Drone",

    # Gerais
    "Aceitação Cibernética", "Conexão Neural", "Resistência a Vácuo", "Ciborgue", "Adaptado à Gravidade Zero"
]

EQUIPAMENTOS_SCIFI = [
    # Cibernéticos
    "Braço Cibernético", "Perna Cibernética", "Pernas Cibernéticas", "Blindagem Dérmica",
    "Olhos Biônicos", "Conector Neural", "Interface de Dados", "Acelerador de Reflexos",
    "Reflexos Ampliados", "Filtro de Toxinas", "Compartimento Interno",

    # Utilitários
    "Ferramenta Multiuso Laser", "Computador de Pulso", "Omni-tool", "Scanner de Longo Alcance",
    "Respirador de Oxigênio", "Tradutor Universal", "Escudo de Energia Portátil",
    "Gerador de Campo de Força",

    # Robótica
    "Drone de Reconhecimento", "Drone de Reparo", "Drone de Combate", "Sentinela de Combate",
    "Robô de Serviço", "IA Portátil", "Drone de Batalha",

    # Armaduras
    "Traje Espacial", "E.V.A.", "Armadura de Polímero", "Armadura de Infantaria Pesada",
    "Traje de Camuflagem Óptica", "Armadura Energizada", "Traje Leve", "Traje Pesado", "Armadura de Infantaria", "Traje de Voo", "Traje de Batalha",

    # Arsenal
    "Pistola Laser", "Rifle Laser", "Gatling Laser", "Pistola de Plasma", "Rifle de Plasma",
    "Arma de Desintegração", "Canhão de Partículas", "Pulseiras de Choque",
    "Arma de Trilho", "Railgun", "Arma de Agulha", "Flechette", "Munição Inteligente", "Girojet",
    "Espada Molecular", "Faca Vibratória", "Vibro-Faca", "Vibro-Espada", "Chicote de Monofilamento", "Maça de Pulso",
    "Espada de Luz", "Espada de Energia", "Laser Pesado",

    # Veículos
    "Carro Voador", "Hovercar", "Moto de Gravidade", "Speeder", "Tanque Flutuador",
    "APC Flutuador", "Moto Chieftain", "Jato Peregrine Jump", "Juggernaut VBTP",
    "Submarino de Ataque Mako", "Tanque de Batalha Grizzly",

    # Mechas
    "Chassis Leve", "Chassis Médio", "Chassis Pesado",

    # Naves
    "Caça Estelar", "Fragata", "Cargueiro Espacial", "Motor de Salto"
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
    name = item.get('nome', item.get('name', ''))
    origem = item.get('origem', '').upper()

    if explicit_origin_check and (origem == 'SCI_FI' or origem == 'CIBERNETICO' or origem == 'MECHA' or origem == 'ARMADURA_ENERGIZADA' or origem == 'ROBO'):
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
            cat_tipo = cat.get('tipo', '').upper()

            # If category is Sci-Fi specific, move all items
            if cat_tipo in ['CIBERNETICO', 'MECHA', 'ROBO', 'ARMADURA_ENERGIZADA'] or cat_origem == 'SCI_FI':
                cat['origem'] = 'SCI_FI'
                for item in cat.get('itens', []):
                    item['origem'] = 'SCI_FI'
                moved_items.append(cat)
                continue

            cat_moved_items = []
            cat_kept_items = []

            for item in cat.get('itens', []):
                item_origem = item.get('origem', cat_origem).upper()
                if (item_origem == 'SCI_FI') or matches_any(item.get('nome',''), ref_list):
                    item['origem'] = 'SCI_FI'
                    cat_moved_items.append(item)
                else:
                    cat_kept_items.append(item)

            if cat_moved_items:
                new_cat = cat.copy()
                new_cat['itens'] = cat_moved_items
                new_cat['origem'] = 'SCI_FI'
                moved_items.append(new_cat)

            if cat_kept_items:
                cat['itens'] = cat_kept_items
                kept_items.append(cat)
    else:
        is_wrapped = isinstance(data, dict) and 'pericias' in data
        items = data['pericias'] if is_wrapped else data

        for item in items:
            if should_move(item, ref_list):
                item['origem'] = 'SCI_FI'
                moved_items.append(item)
            else:
                kept_items.append(item)

        if is_wrapped:
            kept_items = {'pericias': kept_items}

    if moved_items:
        # If dest file exists, merge
        existing = load_json(dest_path)
        if isinstance(existing, list):
            existing.extend(moved_items)
            save_json(existing, dest_path)
        else:
            save_json(moved_items, dest_path)
        print(f"Moved {len(moved_items)} items to {dest_path}")

    save_json(kept_items, source_path)
    print(f"Updated {source_path} with remaining items.")

def merge_separate_files():
    # Merge ciberneticos.json and chassis_sci_fi.json into scifi_equipamentos.json
    equip_path = 'app/src/main/assets/scifi_equipamentos.json'

    files_to_merge = [
        'app/src/main/assets/ciberneticos.json',
        'app/src/main/assets/chassis_sci_fi.json'
    ]

    merged_data = load_json(equip_path)
    if not isinstance(merged_data, list): merged_data = []

    for fpath in files_to_merge:
        data = load_json(fpath)
        if data:
            for cat in data:
                cat['origem'] = 'SCI_FI'
                for item in cat.get('itens', []):
                    item['origem'] = 'SCI_FI'
            merged_data.extend(data)
            print(f"Merged {fpath} into {equip_path}")
            # We don't delete them yet, but we will remove them from DataLoader later

    save_json(merged_data, equip_path)

def main():
    # Ancestralidades
    process_file(
        'app/src/main/assets/suplementos_listaancestralidade.json',
        'app/src/main/assets/scifi_listaancestralidade.json',
        ANCESTRALIDADES_SCIFI
    )

    # Complicacoes
    process_file(
        'app/src/main/assets/suplementos_complicacoes.json',
        'app/src/main/assets/scifi_complicacoes.json',
        COMPLICACOES_SCIFI
    )

    # Vantagens
    process_file(
        'app/src/main/assets/suplementos_vantagens.json',
        'app/src/main/assets/scifi_vantagens.json',
        VANTAGENS_SCIFI
    )

    # Equipamentos
    process_file(
        'app/src/main/assets/suplementos_equipamentos.json',
        'app/src/main/assets/scifi_equipamentos.json',
        EQUIPAMENTOS_SCIFI,
        is_equip=True
    )

    merge_separate_files()

if __name__ == "__main__":
    main()
