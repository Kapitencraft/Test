Preview (Existenz check)
File -> Decl //applyConstructor
Create Skeletons (method signature, field types...) //generateSkeletons
generate method contents //construct
Abschließen //generateClass
Speichern //cache

# Titel
Scripted: Entwicklung einer Programmiersprache als Werkzeug für Minecraft

# Überblick
Scripted ist als Minecraft-Mod entstanden, 
    in der Hoffnung,
    den Spielern die Möglichkeit zu geben, 
    eigene Skripte zu schreiben und so Einfluss auf das Spiel zu nehmen. 
Dazu habe ich die Sprache erst definiert, 
    einen Compiler, 
        der Quellcode in Bytecode übertragen kann
    und eine VirtualMachine geschrieben,
        die den übertragenen Bytecode ausführen kann.
Aus dem Abstract-Syntax-Tree-Programm, 
    welches Schwerpunkt meiner letzten Arbeit war,
    ist nun ein Bytecode-Programm geworden.
Computer laden aus dem RAM immer auch einen kleinen Teil aus der Umgebung der benötigten Daten in den CPU-Cache. 
Werden dann diese Daten von der CPU benötigt, 
    läuft das Programm um einiges schneller.
Für Scripted untersuche ich, ob
    der Bytecode,
        der,
            im Vergleich zum Abstract-Syntax-Tree Programm, 
        generell eine sehr viel höhere Datendichte hat, 
    erwartungsgemäß deutlich schneller läuft und wie ich es noch weiter verbessern kann.
In der Zukunft sollen auch ein Editor und ein Debugger hinzukommen,
    um es Benutzern einfacher zu machen,
        den Code zu schreiben und Fehler zu vermeiden.

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