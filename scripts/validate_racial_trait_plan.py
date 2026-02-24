#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
seed_path = ROOT / "docs" / "racial_trait_catalog_seed.json"
schema_path = ROOT / "docs" / "racial_trait_schema_v1.json"
plan_path = ROOT / "app" / "src" / "main" / "assets" / "racial_trait_execution_plan_v1.json"
report_path = ROOT / "docs" / "racial_trait_execution_report.md"

seed = json.loads(seed_path.read_text(encoding="utf-8"))
schema = json.loads(schema_path.read_text(encoding="utf-8"))
plan = json.loads(plan_path.read_text(encoding="utf-8"))

trait_ids = {t["id"] for t in seed.get("traits", [])}
missing = [tid for tid in plan.get("critical_trait_ids", []) if tid not in trait_ids]

phase_statuses = [p.get("status") for p in plan.get("phases", [])]
all_completed = all(status == "completed" for status in phase_statuses)

errors = []
if seed.get("version") != 1:
    errors.append("Seed com versão inválida")
if schema.get("title") != "Racial Trait Typed Model v1":
    errors.append("Schema inesperado")
if not all_completed:
    errors.append("Há fases não concluídas no plano")
if missing:
    errors.append("Traits críticos ausentes no seed: " + ", ".join(missing))

status = "PASS" if not errors else "FAIL"

report_lines = [
    "# Relatório de Execução do Plano (v1)",
    "",
    f"- Status geral: **{status}**",
    f"- Seed: `{seed_path.relative_to(ROOT)}`",
    f"- Schema: `{schema_path.relative_to(ROOT)}`",
    f"- Plano (asset): `{plan_path.relative_to(ROOT)}`",
    "",
    "## Verificações",
    f"- Fases concluídas: {'sim' if all_completed else 'não'}",
    f"- Traits críticos ausentes: {', '.join(missing) if missing else 'nenhum'}",
]

if errors:
    report_lines.extend(["", "## Erros"] + [f"- {e}" for e in errors])

report_path.write_text("\n".join(report_lines) + "\n", encoding="utf-8")

if errors:
    raise SystemExit(1)

print("PASS")
