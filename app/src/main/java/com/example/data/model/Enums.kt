package com.example.data.model

enum class TaskStatus(val label: String, val badgeColorHex: Long) {
    TODO("Todo", 0xFF94A3B8),
    IN_PROGRESS("In Progress", 0xFF38BDF8),
    IN_REVIEW("In Review", 0xFFA855F7),
    DONE("Completed", 0xFF10B981)
}

enum class Priority(val label: String, val level: Int, val colorHex: Long) {
    P0("P0 Critical", 0, 0xFFEF4444),
    P1("P1 High", 1, 0xFFF97316),
    P2("P2 Medium", 2, 0xFFF59E0B),
    P3("P3 Low", 3, 0xFF64748B)
}

enum class TaskCategory(val label: String, val iconTag: String) {
    FEATURE("Feature", "feat"),
    BUGFIX("Bugfix", "fix"),
    REFACTOR("Refactor", "refactor"),
    DEVOPS("DevOps", "infra"),
    DOCS("Docs", "docs"),
    CODE_REVIEW("Code Review", "review"),
    TESTING("Testing", "test")
}

enum class SprintType(val label: String, val defaultMinutes: Int) {
    DEEP_FOCUS("Deep Focus", 25),
    EXTENDED_HACK("Extended Hack", 50),
    CODE_REVIEW("Code Review", 15),
    QUICK_BREAK("Quick Break", 5)
}
