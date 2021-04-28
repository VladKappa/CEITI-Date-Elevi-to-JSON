# CEITI Date Elevi to JSON
Acest proiect transforma datele obtinute in urma requestului catre http://api.ceiti.md/date in format JSON.

Poate fi folositor pentru un format mai usor de manipulat al datelor. Sau ca punct de inceput pentru a face acest lucru in alt limbaj de programare decat JAVA.

# Cum sa compilam programul
Putem compila foarte usor cu ajutorul Maven-ului. Executa comanda `mvn compile assembly:single`, aceasta ulterior va compila programul nostru intr-un fisier `.jar`.

# Cum sa rulam programul
Pentru a rula programul putem folosi `java -jar Program.jar` in folderul `target`.
