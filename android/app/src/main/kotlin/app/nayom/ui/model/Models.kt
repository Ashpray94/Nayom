package app.nayom.ui.model

/** Mirrors the vault + /reference rules. Status words are firm; wording lives in the spec. */
enum class PromiseState { Open, Done, Missed, Released }
data class Promise(val id: String, val who: String, val what: String, val due: String, val state: PromiseState)
enum class AskKind { Yes, Later, No }
data class Verdict(val ask: String, val kind: AskKind, val reason: String)
