# Triage Labels

GitHub Issues use these lifecycle labels:

| Role | Label | Use |
|---|---|---|
| Needs evaluation | `needs-triage` | All newly created issues awaiting review, reproduction, or scope validation. |
| Needs reporter input | `needs-info` | Missing reproduction details, clarification, or non-sensitive information. |
| Ready for an agent | `ready-for-agent` | Scope and acceptance criteria are clear enough for autonomous implementation. |
| Ready for a human | `ready-for-human` | Requires a human decision, physical-device test, credential rotation, or other human-only action. |
| Will not fix | `wontfix` | Invalid, rejected, or out-of-scope work. |

Security findings additionally receive the `security` label and are prioritized immediately. When transitioning labels, remove the prior lifecycle label so an issue has one lifecycle state.
