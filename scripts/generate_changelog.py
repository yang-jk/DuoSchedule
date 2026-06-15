#!/usr/bin/env python3
"""
从 Product-Spec-CHANGELOG.md 自动提取更新日志，生成 ChangelogData.kt

支持两种格式：
1. 新格式：## [version] - date  +  - [优化/修复/重大] description
2. 旧格式：## [version] - date  +  ### 变更类型  +  **bold summary**
"""

import re
import sys

CHANGELOG_FILE = "Product-Spec-CHANGELOG.md"
OUTPUT_FILE = "app/src/main/java/com/duoschedule/ui/settings/ChangelogData.kt"

TAG_MAP = {
    "优化": "FEATURE",
    "新增": "ADDITION",
    "修复": "BUGFIX",
    "重大": "BREAKING",
}

OLD_TYPE_MAP = {
    "功能增强": "FEATURE",
    "功能优化": "FEATURE",
    "功能更新": "FEATURE",
    "性能优化": "FEATURE",
    "Bug修复": "BUGFIX",
    "Bug 修复": "BUGFIX",
    "重大变更": "BREAKING",
}


def parse_changelog(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    entries = []
    # Match version headers: ## [version] - date
    version_pattern = re.compile(
        r'^##\s*\[(\d+\.\d+\.\d+)\]\s*-\s*(\d{4}-\d{2}-\d{2})\s*$',
        re.MULTILINE
    )

    matches = list(version_pattern.finditer(content))

    for i, match in enumerate(matches):
        version = match.group(1)
        date = match.group(2)

        # Extract body until next version header or end of file
        body_start = match.end()
        body_end = matches[i + 1].start() if i + 1 < len(matches) else len(content)
        body = content[body_start:body_end]

        items = parse_new_format_items(body)

        if not items:
            items = parse_old_format_items(body, version)

        if items:
            entries.append({
                "version": version,
                "date": date,
                "items": items,
            })

    return entries


def parse_new_format_items(body):
    """Parse - [优化/修复/重大] description lines"""
    items = []
    item_pattern = re.compile(r'^-\s*\[(优化|新增|修复|重大)\]\s*(.+)$', re.MULTILINE)

    for match in item_pattern.finditer(body):
        tag = match.group(1)
        description = match.group(2).strip().replace('"', '\\"')
        changelog_type = TAG_MAP.get(tag, "FEATURE")
        items.append({
            "type": changelog_type,
            "summary": description,
        })

    return items


def parse_old_format_items(body, version):
    """Parse old format: ### 变更类型 + **bold summary**"""
    items = []

    # Get change type
    change_type = "FEATURE"
    type_match = re.search(r'###\s*变更类型[：:]\s*(.+)', body)
    if type_match:
        raw_type = type_match.group(1).strip()
        change_type = OLD_TYPE_MAP.get(raw_type, "FEATURE")

    # Get bold summary
    summary_match = re.search(r'\*\*(.+?)\*\*', body)
    if summary_match:
        summary = summary_match.group(1).strip().replace('"', '\\"')
        items.append({
            "type": change_type,
            "summary": summary,
        })
    else:
        items.append({
            "type": change_type,
            "summary": f"版本 {version} 更新",
        })

    return items


def generate_kotlin(entries):
    lines = []
    lines.append("package com.duoschedule.ui.settings")
    lines.append("")
    lines.append("// 自动生成 - 请勿手动修改 - 运行 scripts/generate_changelog.py 更新")
    lines.append("val changelogEntries = listOf(")

    for entry in entries:
        lines.append("    ChangelogEntry(")
        lines.append(f'        version = "{entry["version"]}",')
        lines.append(f'        date = "{entry["date"]}",')
        lines.append("        items = listOf(")

        for item in entry["items"]:
            lines.append(f'            ChangelogItem(ChangelogType.{item["type"]}, "{item["summary"]}"),')

        lines.append("        )")
        lines.append("    ),")

    lines.append(")")
    lines.append("")

    return "\n".join(lines)


if __name__ == "__main__":
    entries = parse_changelog(CHANGELOG_FILE)
    if not entries:
        print(f"Warning: No changelog entries found in {CHANGELOG_FILE}", file=sys.stderr)
        sys.exit(1)

    kotlin_code = generate_kotlin(entries)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(kotlin_code)

    print(f"Generated {OUTPUT_FILE} with {len(entries)} entries")
