Preview (Existenz check)
File -> Decl //applyConstructor
Create Skeletons (method signature, field types...) //generateSkeletons
generate method contents //construct
Abschließen //generateClass
Speichern //cache

# Titel
Scripted: Entwicklung einer Programmiersprache als Werkzeug für Minecraft

# Überblick
Scripted entstand als Minecraft-Mod, 
    in der Hoffnung,
        den Spielern die Möglichkeit zu geben, 
    eigene Skripte zu schreiben und so Einfluss auf das Spiel zu nehmen.
Aus dem Programm, 
    welches Schwerpunkt meiner letzten Arbeit war und einen Graphen zur Speicherung verwendete,
        ist nun ein Bytecode-Programm geworden.
Dazu habe ich die Sprache erst definiert,
    einen Compiler, der Quellcode in Anweisungen übertragen kann, geschrieben und anschließend
    ein Programm geschrieben,
        die den übertragenen Bytecode ausführen kann.
Für Scripted untersuche ich, ob
    der Programmcode,
        der,
            im Vergleich zum Graphen-Programm, 
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