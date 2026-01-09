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
        # Handle cases like "Atraente (e Muito)" mapping to "Atraente" and "Muito Atraente"
        # But here I will use explicit mapping in the list for safety.
    return False

# --- LISTS ---

ANCESTRALIDADES_CORE = [
    "Humanos", "Humano", "Aquarianos", "Aquariano", "Avianos", "Aviano",
    "Anões", "Anão", "Elfos", "Elfo", "Meio-Elfos", "Meio-Elfo",
    "Meio-Orcs", "Meio-Orc", "Rakashas", "Rakashanos", "Rakashan",
    "Saurianos", "Sáurios", "Sauriano", "Sáurio"
]

PERICIAS_CORE = [
    "Atletismo", "Atirar", "Ciência", "Conhecimento Acadêmico", "Consertar",
    "Curar", "Dirigir", "Eletrônica", "Fé", "Foco", "Furtividade", "Idiomas",
    "Intimidar", "Jogar", "Ladinagem", "Lutar", "Navegar", "Ocultismo",
    "Perceber", "Performance", "Persuadir", "Pesquisar", "Pilotar",
    "Provocar", "Psionismo", "Psiônicos", "Sobrevivência", "Sortilégio", "Conjurar"
]

COMPLICACOES_CORE = [
    "Anêmico", "Arrogante", "Boca Aberta", "Boca Grande", "Canhestro", "Atrapalhado",
    "Cego", "Código de Honra", "Curioso", "Deficiente Físico", "Delirante",
    "Dependente", "Difícil de Curar", "Excesso de Confiança", "Ganancioso",
    "Forasteiro", "Humilde", "Pobreza", "Impulsivo", "Inimigo", "Jovem",
    "Juramento", "Voto", "Lento", "Má Fama", "Procurado", "Muito Jovem",
    "Mudo", "Nanismo", "Pequeno", "Obeso", "Pacifista", "Peculiaridade",
    "Pesado de Ouvido", "Deficiente Auditivo", "Sanguinário", "Teimoso",
    "Vício", "Hábito", "Vingativo"
]

VANTAGENS_CORE = [
    # Combate
    "Atirador", "Ataque de Oportunidade", "Atacar Primeiro", "Atacar Primeiro Aprimorado",
    "Bloquear", "Bloquear Aprimorado",
    "Contra-Ataque", "Contra-Ataque Aprimorado",
    "Esquiva", "Esquiva Aprimorada",
    "Estilo de Luta", "Artista Marcial", # Assuming mapping
    "Firmeza", "Nervos de Aço", "Nervos de Aço Aprimorados",
    "Flanquear", "Frenesi", "Frenesi Aprimorado",
    "Golpe Fulminante", "Impiedoso", # Assuming mapping
    "Lutar com Duas Armas",
    "Mata-Gigantes", "Matador de Gigantes",
    "Primeiro Golpe", # Same as Atacar Primeiro
    "Prontidão",
    "Provocador",
    "Recarga Rápida", "Recarga Rápida Aprimorado",
    "Retirada Cautelosa", "Retirada", "Retirada Aprimorada",
    "Sacar Rápido", # Check if exists
    "Sangue Frio", "Calculista", # Assuming mapping based on description
    "Soldado",
    "Varredura", "Varredura Aprimorada",

    # Liderança
    "Comando", "Presença de Comando", "Fervor", "Inspirar",
    "Manter a Formação", "Mantenham a Formação!",
    "Natural", "Líder Nato",

    # Sociais
    "Atraente", "Muito Atraente",
    "Carismático",
    "Conexões",
    "Destemido", "Obstinado", "Vontade de Ferro", # Assuming mappings
    "Humilhar",
    "Musculoso",
    "Obter Informações", "Investigador",

    # Profissionais
    "Acadêmico", "Erudito",
    "Artífice",
    "Ás",
    "Batedor",
    "Ladrão",
    "Mateiro",
    "McGyver",
    "Médico", "Cirurgião", # Check mapping

    # Estranhas
    "Curandeiro",
    "Sorte", "Sorte Grande",
    "Resiliência", "Duro na Queda", "Muito Duro na Queda",
    "Sentido Aguçado", "Noção do Perigo",

    # Poderes
    "Antecedente Arcano", "Antecedente Arcano (Dom)", "Antecedente Arcano (Magia)", "Antecedente Arcano (Milagres)", "Antecedente Arcano (Psiônicos)", "Antecedente Arcano (Ciência Estranha)",
    "Canalizador", "Canalização",
    "Novo Poder", "Novos Poderes",
    "Pontos de Poder",
    "Recuperação de Poderes", "Recuperação Rápida" # Check mapping
]

EQUIPAMENTOS_CORE = [
    # Aventura
    "Algemas", "Aljava", "Apito", "Binóculos", "Bússola", "Cantil", "Odre",
    "Corda", "Gancho", "Gancho de Escalada", "Gazuas", "Kit de Ferramentas",
    "Kit Médico", "Kit de Primeiros Socorros", "Lanterna", "Mochila",
    "Pederneira", "Pederneira e Aço", "Pé de Cabra", "Sabão", "Tocha", "Vela",

    # Armaduras
    "Jaqueta de Couro", "Calças de Couro", "Jaqueta", "Calças", "Couro",
    "Camisa de Malha", "Calças de Malha", "Capuz de Cota",
    "Corselete de Bronze", "Braçadeiras de Bronze", "Grevas de Bronze", "Elmo de Bronze",
    "Placas", "Corselete de Placas", "Braçadeiras de Placas", "Grevas de Placas", "Elmo Pesado",
    "Jaqueta Kevlar", "Colete de Kevlar", "Colete Kevlar",
    "Traje Antibombas",

    # Escudos
    "Pequeno", "Médio", "Grande", "Escudo Pequeno", "Escudo Médio", "Escudo Grande",
    "Anti-Tumulto", "Escudo Anti Tumulto", "Balístico", "Escudo Balístico",
    "Medieval Pequeno", "Medieval Médio", "Medieval Grande",

    # Armas Brancas
    "Adaga", "Faca", "Bastão", "Baioneta", "Cassetete", "Porrete", "Clava",
    "Espada", "Espada Curta", "Espada Longa", "Espada Bastarda", "Espada Grande",
    "Lança", "Maça", "Machado", "Machado de Mão", "Machado de Batalha", "Machado Grande",
    "Malho", "Mangual", "Martelo de Guerra", "Picareta", "Pique",
    "Rapieira", "Sabre", "Soqueira", "Soqueiras", "Motosserra",

    # Armas à Distância
    "Arco", "Arco Curto", "Arco Longo", "Arco Composto",
    "Besta", "Besta Leve", "Besta Pesada",
    "Funda", "Arremesso", "Machado de Arremesso",

    # Armas de Fogo
    "Derringer", "Revólver", ".38", ".357", ".45", "Colt Peacemaker", "Revólver Policial", "Smith & Wesson",
    "Semi-automática", "9mm", ".50", "Glock", "Colt 1911", "Desert Eagle",
    "Thompson", "Tommy Gun", "MP5", "Uzi",
    "AK47", "M16", "Steyr AUG",
    "Escopeta", "Cano Duplo", "Pump", "Pump Action", "Assalto", "Espingarda de Assalto",
    "Rifle", "Winchester", "M1 Garand", "Barrett", "Barrett .50",
    "Metralhadora", "Browning", "M60", "Gatling", "Minigun", "SAW",

    # Veículos
    "Bicicleta",
    "Carro", "Carro Compacto", "Carro Médio", "Carro Esportivo",
    "Motocicleta", "Motocicleta de Rua", "Motocicleta de Motocross",
    "Jeep", "Jipe",
    "Caminhão",
    "Tanque Sherman", "M4 Sherman",
    "Cessna", "Cessna Skyhawk",
    "Helicóptero", "Helicóptero Apache", "AH-64 Apache",
    "Learjet",
    "Galeão",
    "Barco a Remo",
    "Lancha"
]

def load_json(path):
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        return None

def save_json(data, path):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def process_simple_list(filename, core_list, name_field='nome'):
    data = load_json(f'app/src/main/assets/{filename}')
    if not data: return

    # If data is dict with list inside (like pericias.json)
    is_wrapped = isinstance(data, dict)
    items = data['pericias'] if is_wrapped and 'pericias' in data else data

    core_items = []
    supplement_items = []

    for item in items:
        name = item.get(name_field, "")
        if matches_any(name, core_list):
            item['origem'] = "BASICO"
            core_items.append(item)
        else:
            # If origin is not basic, keep it. If missing, set to SUPLEMENTO or leave as is?
            # User said "isolate Core SWADE".
            if item.get('origem', '').upper() == 'BASICO':
                 # Even if marked BASIC, if not in list, move to supplements (per user instruction)
                 item['origem'] = "SUPLEMENTO"
            supplement_items.append(item)

    # Save files
    base_name = filename.lower().replace('.json', '')

    if is_wrapped:
        save_json({ 'pericias': core_items }, f'app/src/main/assets/basico_{base_name}.json')
        save_json({ 'pericias': supplement_items }, f'app/src/main/assets/suplementos_{base_name}.json')
    else:
        save_json(core_items, f'app/src/main/assets/basico_{base_name}.json')
        save_json(supplement_items, f'app/src/main/assets/suplementos_{base_name}.json')

def process_equipamentos():
    data = load_json('app/src/main/assets/equipamentos.json')
    if not data: return

    core_cats = []
    supp_cats = []

    for cat in data:
        cat_core_items = []
        cat_supp_items = []

        for item in cat.get('itens', []):
            name = item.get('nome', "")
            # Check against Equip list
            if matches_any(name, EQUIPAMENTOS_CORE):
                item['origem'] = "BASICO"
                cat_core_items.append(item)
            else:
                if item.get('origem', '').upper() == 'BASICO':
                    item['origem'] = "SUPLEMENTO"
                cat_supp_items.append(item)

        if cat_core_items:
            new_cat = cat.copy()
            new_cat['itens'] = cat_core_items
            new_cat['origem'] = "BASICO"
            core_cats.append(new_cat)

        if cat_supp_items:
            new_cat = cat.copy()
            new_cat['itens'] = cat_supp_items
            # Don't overwrite category origin if it was something else, but if it was BASICO, maybe change?
            # Category origin is used for filtering in UI.
            # If I split the items, the category shell needs to exist in both.
            supp_cats.append(new_cat)

    save_json(core_cats, 'app/src/main/assets/basico_equipamentos.json')
    save_json(supp_cats, 'app/src/main/assets/suplementos_equipamentos.json')

def main():
    process_simple_list('pericias.json', PERICIAS_CORE, 'nome')
    process_simple_list('listaancestralidade.json', ANCESTRALIDADES_CORE, 'nome')
    process_simple_list('complicacoes.json', COMPLICACOES_CORE, 'name') # Note 'name' vs 'nome'
    process_simple_list('Vantagens.json', VANTAGENS_CORE, 'nome')
    process_equipamentos()

if __name__ == "__main__":
    main()
