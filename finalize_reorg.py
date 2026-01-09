import json
import os

def fix_equip_supplements():
    path = 'app/src/main/assets/suplementos_equipamentos.json'
    if not os.path.exists(path): return

    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for cat in data:
        cat['origem'] = "SUPLEMENTO"
        # Also update items just in case, though usually category origin rules
        for item in cat.get('itens', []):
            item['origem'] = "SUPLEMENTO"

    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        print("Updated suplementos_equipamentos.json with correct origin.")

def delete_old_files():
    files = [
        'app/src/main/assets/equipamentos.json',
        'app/src/main/assets/Vantagens.json',
        'app/src/main/assets/complicacoes.json',
        'app/src/main/assets/pericias.json',
        'app/src/main/assets/listaancestralidade.json'
    ]
    for f in files:
        if os.path.exists(f):
            os.remove(f)
            print(f"Deleted {f}")

if __name__ == "__main__":
    fix_equip_supplements()
    delete_old_files()
