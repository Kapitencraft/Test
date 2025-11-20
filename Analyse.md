Scripted:
Eine java-ähnliche Programmiersprache und gleichzeitig eine Forge Mod für Minecraft


Preview (Existenz check)
File -> Decl //applyConstructor
Create Skeletons (method signature, field types...) //generateSkeletons
generate method contents //construct
Abschließen //generateClass
Speichern //cache

# Überblick
Scripted ist als Minecraft Mod entstanden, in der Hoffnung, den Spielern die Möglichkeit zu geben, im Spiel dynamisch Funktionen zu schreiben die Einfluss auf das Spiel nehmen können. Aus der Abstract Syntax Tree Compilierten und Interpretierten Implementierung aus dem letzten Jahr ist nun eine Bytecode Compilierte und Interpretierte Implementierung geworden.

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

Change:
<br>added Call expr to each binary expr
<br>Affected Expressions:
<br>**Binary (BinaryCall)**
<br>**Set (SetCall)**
<br>**SpecialSet (SpecialSetCall)**
<br>**StaticSet (StaticSetCall)**
<br>**StaticSpecial (StaticSpecialCall)**
<br>**ArraySet (ArraySetCall)**
<br>**ArraySpecial (ArraySpecialCall)**