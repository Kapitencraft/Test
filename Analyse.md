Scripted:
Eine java-ähnliche Programmiersprache und gleichzeitig eine Forge Mod für Minecraft


Preview (Existenz check)
File -> Decl //applyConstructor
Create Skeletons (method signature, field types...) //generateSkeletons
generate method contents //construct
Abschließen //generateClass
Speichern //cache

# Überblick
Scripted ist als Minecraft-Mod entstanden, 
    in der Hoffnung,
    den Spielern die Möglichkeit zu geben, 
    eigene Skripte zu schreiben und so Einfluss auf das Spiel zu nehmen. 
Hierbei erforsche ich,
    wie Compiler effizienten Bytecode erstellen können. 
Aus dem Abstract-Syntax-Tree-Programm, 
    welches im letzten Jahr eingereicht wurde,
    ist nun ein Bytecode-Programm geworden.
Computer laden aus dem RAM immer auch einen kleinen Teil aus der Umgebung der benötigten Daten in den CPU-Cache, 
    was die Ladezeit jener Daten erheblich erhöht. 
Werden dann diese Daten vom CPU benötigt, 
    läuft das Programm um einiges schneller, 
    was datendichte Programme .
Das führt dazu, 
    dass Bytecode,
        der,
            im Vergleich zum Abstract-Syntax-Tree Programm, 
        generell eine sehr viel höhere Datendichte hat, 
    deutlich schneller läuft.

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
<br>reworked backend to support anonymous classes
<br>Affected Expressions:
<br>**Constructor**

Change:
<br>added Call expr to each binary expr
<br>Affected Expressions:
<br>**Binary**
<br>**Set**
<br>**SpecialSet**
<br>**StaticSet**
<br>**StaticSpecial**
<br>**ArraySet**
<br>**ArraySpecial**

Change:
<br>added an exception to slice to ensure errors are readable and fixable
<br>Affected Expressions:
<br>**Slice**

Change:
<br>ensured the array in for-each statements are not poped each iteration, decreasing its runtime as it is no longer necessary to re-load the value
<br>Affected Expressions:
<br>**ForEach**