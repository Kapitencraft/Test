Scripted:
Eine java-ähnliche Programmiersprache und gleichzeitig eine Forge Mod für Minecraft


Preview (Existenz check)
File -> Decl //applyConstructor
Create Skeletons (method signature, field types...) //generateSkeletons
generate method contents //construct
Abschließen //generateClass
Speichern //cache

# Changes

Change:
    `else if` branches now check if they are cancelled (either via `throw` or `return`) and will not emit a jump if that's the case
Affected Expressions: 
1. If

Change:
    added LNT and LVT
Affected Expressions:
**All**