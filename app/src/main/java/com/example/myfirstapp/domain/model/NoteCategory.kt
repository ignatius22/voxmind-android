package com.example.myfirstapp.domain.model

enum class NoteCategory(val displayName: String, val iconName: String) {
    ALL("All", "Apps"),
    MEETING("Meeting", "Groups"),
    LECTURE("Lecture", "School"),
    BRAINSTORM("Brainstorm", "Psychology"),
    IDEAS("Ideas", "Lightbulb"),
    PERSONAL("Personal", "Person")
}
