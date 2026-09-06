#!/usr/bin/env python3
"""Convert a Qodana SARIF report into a Markdown summary.

Usage: qodana_summary.py <qodana.sarif.json>

Qodana runs the same IntelliJ/Android Studio inspections (unused code,
naming conventions, redundant statements, etc.) that Android Lint and the
Kotlin compiler don't cover. Prints a Markdown table to stdout, suitable
for appending to $GITHUB_STEP_SUMMARY.
"""
import json
import sys
from collections import Counter


def escape(text) -> str:
    if text is None:
        return ""
    return (
        str(text)
        .replace("|", "\\|")
        .replace("\n", " ")
        .replace("\r", " ")
        .strip()
    )


def main(path: str) -> int:
    try:
        with open(path) as f:
            data = json.load(f)
    except (OSError, json.JSONDecodeError):
        print(f"Não foi possível ler o relatório SARIF do Qodana em `{path}`.")
        return 0

    issues = []
    for run in data.get("runs", []):
        rules = {}
        for rule in run.get("tool", {}).get("driver", {}).get("rules", []):
            rules[rule.get("id", "")] = (
                rule.get("shortDescription", {}).get("text")
                or rule.get("name")
                or rule.get("id", "")
            )

        for result in run.get("results", []):
            rule_id = result.get("ruleId", "")
            message = result.get("message", {}).get("text", "")
            level = result.get("level", "warning")
            location = (result.get("locations") or [{}])[0]
            phys = location.get("physicalLocation", {})
            uri = phys.get("artifactLocation", {}).get("uri", "")
            line = phys.get("region", {}).get("startLine", "")
            issues.append(
                {
                    "rule": rules.get(rule_id, rule_id),
                    "rule_id": rule_id,
                    "level": level,
                    "message": message,
                    "file": uri,
                    "line": line,
                }
            )

    if not issues:
        print("Nenhum problema encontrado pelo Qodana. ✅")
        return 0

    level_counts = Counter(i["level"] for i in issues)
    rule_counts = Counter(i["rule"] for i in issues)

    print(f"**Total de ocorrências:** {len(issues)}")
    print()
    print("| Nível | Quantidade |")
    print("|---|---|")
    for level, count in sorted(level_counts.items(), key=lambda kv: -kv[1]):
        print(f"| {escape(level)} | {count} |")
    print()

    print("<details><summary>Top regras (por quantidade)</summary>")
    print()
    print("| Regra | Quantidade |")
    print("|---|---|")
    for rule, count in rule_counts.most_common(30):
        print(f"| {escape(rule)} | {count} |")
    print()
    print("</details>")
    print()

    print("<details><summary>Lista completa</summary>")
    print()
    print("| Nível | Regra | Arquivo | Linha | Mensagem |")
    print("|---|---|---|---|---|")
    for i in sorted(issues, key=lambda x: (x["file"], int(x["line"] or 0))):
        print(
            f"| {escape(i['level'])} | {escape(i['rule'])} | "
            f"`{escape(i['file'])}` | {escape(i['line'])} | {escape(i['message'])} |"
        )
    print()
    print("</details>")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Uso: qodana_summary.py <qodana.sarif.json>", file=sys.stderr)
        sys.exit(1)
    sys.exit(main(sys.argv[1]))
