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
<br>`else if` branches now check if they are cancelled (either via `throw` or `return`) and will not emit a jump if that's the case.
<br>Affected Expressions: 
<br>**If**

Change:
<br>added LineNumberTable and LocalVariableTable
<br>Affected Expressions:
<br>**All**

Change:
<br>reworked backed to support anonymous classes
<br>Affected Expressions:
<br>**Constructor**