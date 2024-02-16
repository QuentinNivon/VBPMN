QUESTION 1:

---REQUETE---

SELECT DISTINCT(nomC)
FROM LesReservations;

---TESTS---

Pas grand chose à tester

QUESTION 2:

---REQUETE : Donner les noms des clients qui ont effectué exactement une réservation---

SELECT nomC
FROM LesReservations
GROUP BY (nomC)
HAVING COUNT(numR) = 1;

Résultat :

NOMC
---------------
Bidochon                                                                                                                            
Bonemine                                                                                                                            
Corto                                                                                                                               
GrosseBaf                                                                                                                           
Jerry                                                                                                                               
La Castafiore                                                                                                                       
Obelix

---TESTS---

SELECT nomC, COUNT(*)
FROM LesReservations
GROUP BY (nomC);

Résultat :

NOMC                                                 COUNT(*)                                                                       
-------------------------------------------------- ----------                                                                       
Obelix                                                      1   -                                                                    
Nestor Burma                                                3                                                                       
La Castafiore                                               1   -                                                                   
Donald                                                      3                                                                       
Picsou                                                      3                                                                       
Ordralphabetix                                              3                                                                       
Milou                                                       2                                                                       
Jerry                                                       1   -                                                                   
Bidochon                                                    1   -                                                                   
Bonemine                                                    1   -                                                                   
Asterix                                                     2                                                                       
Corto                                                       1   -                                                                   
Mafalda                                                     4                                                                       
GrosseBaf                                                   1   -

On a bien le même résultat

QUESTION 3:

---REQUETE : Donner les numéros des circuits où on ne visite pas la ville de départ---

SELECT numC
FROM LesCircuits
WHERE numC NOT IN (
    SELECT C.numC
    FROM LesCircuits C JOIN LesEtapes E ON (C.numC = E.numC AND C.vDep = E.vEtape) 
);

Résultat :

NUMC                                                                                                                                
----                                                                                                                                
   8                                                                                                                                
   1                                                                                                                                
   5                                                                                                                                
  13                                                                                                                                
  16                                                                                                                                
  18                                                                                                                                
   4                                                                                                                                
   9   
  15                                                                                                                                
   6                                                                                                                                
   2                                                                                                                                
   3                                                                                                                                
  21                                                                                                                                
  11                                                                                                                             

---TESTS---

SELECT numC, vDep, vEtape
FROM LesCircuits JOIN LesEtapes USING (numC);

Résultat :

NUMC VDEP                                     VETAPE                                                                                
---- ---------------------------------------- ----------------------------------------                                              
   1 Paris                                    Londres                                                                               
   2 Paris                                    Londres                                                                               
   3 Paris                                    Amsterdam                                                                             
   4 Paris                                    Amsterdam                                                                             
   5 Lyon                                     Venise                                                                                
   6 Paris                                    Venise                                                                                
   7 Quiberon                                 Port Maria                                                                            
   7 Quiberon                                 Port Cotton   
   7 Quiberon                                 Quiberon                                                                              
   7 Quiberon                                 Houat                                                                                 
   7 Quiberon                                 Hoedic                                                                                
   8 Clermont Ferrand                         Orcival                                                                               
   8 Clermont Ferrand                         Laschamps                                                                             
   8 Clermont Ferrand                         Besse                                                                                 
   9 Paris                                    St Ives                                                                               
   9 Paris                                    Bath                                                                                  
   9 Paris                                    Exeter                                                                                
   9 Paris                                    Londres
   9 Paris                                    Sissinghurst                                                                          
   9 Paris                                    Salisbury                                                                             
  10 Shannon                                  Kenmare                                                                               
  10 Shannon                                  Killarney                                                                             
  10 Shannon                                  Dingle                                                                                
  10 Shannon                                  Shannon                                                                               
  10 Shannon                                  Glengariff                                                                            
  10 Shannon                                  Cork                                                                                  
  10 Shannon                                  Bantry                                                                                
  11 Shannon                                  Cleggan
  11 Shannon                                  Clifden                                                                               
  11 Shannon                                  Galway                                                                                
  11 Shannon                                  Leenan                                                                                
  11 Shannon                                  Louisburgh                                                                            
  11 Shannon                                  Westport                                                                              
  11 Shannon                                  Ennis                                                                                 
  12 Dublin                                   Carrick                                                                               
  12 Dublin                                   Killibegs                                                                             
  12 Dublin                                   Donegal                                                                               
  12 Dublin                                   Ardara 
  12 Dublin                                   Dublin                                                                                
  12 Dublin                                   Letterkenny                                                                           
  12 Dublin                                   Dungloe                                                                               
  13 Paris                                    Copenhague                                                                            
  13 Paris                                    Jakobshavn                                                                            
  13 Paris                                    Christianshab                                                                         
  13 Paris                                    Godhavn                                                                               
  13 Paris                                    Egedesmine                                                                            
  13 Paris                                    Holsteinborg                                                                          
  13 Paris                                    Disko
  13 Paris                                    Sondre                                                                                
  14 Reykjavik                                Reykjavik                                                                             
  14 Reykjavik                                Gullfoss                                                                              
  14 Reykjavik                                Geysir                                                                                
  14 Reykjavik                                Hveravellir                                                                           
  14 Reykjavik                                Akureyri                                                                              
  14 Reykjavik                                Myvatn                                                                                
  14 Reykjavik                                Asbyrgi                                                                               
  15 Paris                                    Husavik                                                                               
  15 Paris                                    Reykholt
  15 Paris                                    Reykjavik                                                                             
  15 Paris                                    Godafoss                                                                              
  16 Paris                                    Hammerfest                                                                            
  16 Paris                                    Inari                                                                                 
  16 Paris                                    Salla                                                                                 
  16 Paris                                    Cap Nord                                                                              
  16 Paris                                    Tornio                                                                                
  16 Paris                                    Saarijarvi                                                                            
  16 Paris                                    Helsinky                                                                              
  16 Paris                                    Ylivieska
  17 Lisbonne                                 Lisbonne                                                                              
  17 Lisbonne                                 Faro                                                                                  
  17 Lisbonne                                 Evora                                                                                 
  17 Lisbonne                                 Castelo de Vide                                                                       
  17 Lisbonne                                 Fatima                                                                                
  17 Lisbonne                                 Leiria                                                                                
  17 Lisbonne                                 Porto                                                                                 
  17 Lisbonne                                 Vila Real                                                                             
  17 Lisbonne                                 Urgeirica                                                                             
  18 Paris                                    Verone
  18 Paris                                    Ravenne                                                                               
  18 Paris                                    Florence                                                                              
  18 Paris                                    Rome                                                                                  
  18 Paris                                    Venise                                                                                
  19 Rome                                     Rome                                                                                  
  19 Rome                                     Caserte                                                                               
  19 Rome                                     Benevento                                                                             
  19 Rome                                     Bari                                                                                  
  19 Rome                                     Brindisi                                                                              
  19 Rome                                     Lecce
  19 Rome                                     Metaponto                                                                             
  19 Rome                                     Catanzaro                                                                             
  19 Rome                                     Cosenza                                                                               
  19 Rome                                     Salerne                                                                               
  19 Rome                                     Capri                                                                                 
  19 Rome                                     Pompei                                                                                
  19 Rome                                     Naples                                                                                
  20 Briancon                                 Briancon                                                                              
  21 Paris                                    Ravenne                                                                               
  21 Paris                                    Florence
  21 Paris                                    Rome                                                                                  
  21 Paris                                    Saarijarvi                                                                            
  21 Paris                                    Venise                                                                                
  21 Paris                                    Inari                                                                                 
  21 Paris                                    Hammerfest                                                                            
  21 Paris                                    Cap Nord                                                                              
  21 Paris                                    Tornio                                                                                
  21 Paris                                    Ylivieska                                                                             
  21 Paris                                    Helsinky                                                                              
  21 Paris                                    Verone 
  21 Paris                                    Salla

On regarde pour chaque circuit si on retrouve la ville de départ dans la liste des villes étapes du circuit

SELECT DISTINCT LesCircuits.numC
FROM LesCircuits JOIN LesEtapes ON (vDep = vEtape);

Résultat :

NUMC                                                                                                                                
----                                                                                                                                
   7                                                                                                                                
  12                                                                                                                                
  17                                                                                                                                
  20                                                                                                                                
  14                                                                                                                                
  19                                                                                                                                
  11                                                                                                                                
  10

On récupère tous les numéros de circuit pour lesquels la ville de départ est une ville étape et on compare à notre résultat.
On doit avoir une intersection vide.

QUESTION 4:

---REQUETE : Donner les noms des clients qui ont réservé un circuit dont la 2ème étape propose un séjour de minimum 2 jours---

SELECT DISTINCT nomC
FROM LesReservations JOIN LesEtapes USING(numC)
WHERE rang=2 AND nbJours>=2

Résultat :

NOMC                                                                                                                                
--------------------------------------------------                                                                                  
Obelix                                                                                                                              
Nestor Burma                                                                                                                        
La Castafiore                                                                                                                       
Donald                                                                                                                              
Picsou                                                                                                                              
Milou                                                                                                                               
Ordralphabetix                                                                                                                      
Bidochon                                                                                                                            
Bonemine                                                                                                                            
Corto                                                                                                                               
Mafalda

---TESTS---

SELECT DISTINCT numC
FROM LesEtapes
WHERE rang = 2 AND nbJours >= 2;

On récupère tous les numéros de circuit dont la 2ème étape propose un séjour de minimum 2 jours

NUMC                                                                                                                                
----                                                                                                                                
   7
   8
   9
  10      
  11                                                                                                                                
  12                                                                                                                                
  13                                                                                                                                
  14                                                                                                                                
  15
  18
  
SELECT DISTINCT nomC
FROM LesReservations
WHERE numC IN (7,8,9,10,11,12,13,14,15,18);

On récupère les noms des clients qui ont reservé un de ces circuits:

NOMC                                                                                                                                
--------------------------------------------------                                                                                  
Obelix                                                                                                                              
Nestor Burma                                                                                                                        
La Castafiore                                                                                                                       
Donald                                                                                                                              
Picsou     
Milou                                                                                                                               
Ordralphabetix                                                                                                                      
Bidochon                                                                                                                            
Bonemine                                                                                                                            
Corto                                                                                                                               
Mafalda  

On compare avec le résultat de notre requête                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       

QUESTION 5:

---REQUETE: Donner l'ensemble de villes par lesquelles le circuit 1 passe (indépendamment du fait que la ville soit visitée où pas). Afficher le résultat dans une seule colonne "ville"---

SELECT vDep AS ville
FROM LesCircuits
WHERE numC=1
UNION
SELECT vArr AS ville
FROM LesCircuits
WHERE numC=1
UNION
SELECT vEtape AS ville
FROM LesEtapes
WHERE numC=1

Résultat :

VILLE                                                                                                                               
--------------------                                                                                                                
Londres                                                                                                                             
Paris

---TESTS---

Pas grand chose à tester

QUESTION 6:

---REQUETE : Pour chacune des villes visitées par Milou, donner le prix total pour visiter tous les monuments de cette ville (ce prix doit être considéré comme 0 s'il n'y a aucun monument visité)---

WITH VillesVisiteesParMilou AS (
    SELECT nomV
    FROM LesVilles
    WHERE nomV IN (
        SELECT vEtape
        FROM LesEtapes JOIN LesReservations USING (numC)
        WHERE nomC = 'Milou'
    )
)
SELECT V.nomV, NVL(SUM(prix),0) AS prixTotMon
FROM VillesVisiteesParMilou V LEFT JOIN LesMonuments M ON V.nomV = M.nomV
GROUP BY V.nomV;

Résultat :

NOMV                                     PRIXTOTMON                                                                                 
---------------------------------------- ----------                                                                                 
Laschamps                                         0                                                                                 
Besse                                             0                                                                                 
Londres                                         200                                                                                 
Orcival                                           0

---TESTS---

On récupère la liste des villes visitées par Milou :

SELECT DISTINCT vEtape
FROM LesEtapes JOIN LesReservations USING (numC)
WHERE nomC = 'Milou';

VETAPE                                                                                                                              
----------------------------------------                                                                                            
Laschamps                                                                                                                           
Besse                                                                                                                               
Londres                                                                                                                             
Orcival

On regarde les villes qui ont des monuments parmi ces villes :

SELECT DISTINCT nomV
FROM LesMonuments
WHERE nomV IN ('Laschamps', 'Besse', 'Londres', 'Orcival');

NOMV                                                                                                                                
----------------------------------------                                                                                            
Londres

On récupère le prix de tous les monuments de Londres (les autres villes ont un prix à 0)

SELECT SUM(prix)
FROM LesMonuments
WHERE nomV = 'Londres'
GROUP BY nomV;

SUM(PRIX)                                                                                                                          
----------                                                                                                                          
       200

On vérifie que ça correspond bien à notre résultat

QUESTION 7:

---REQUETE : Donner le numéro, le nombre total de jours (durée), et le prix de base des circuits qui n'ont aucune réservation---

SELECT numC, SUM(nbJours) AS duree, prix
FROM LesCircuits JOIN LesEtapes USING (numC)
WHERE numC NOT IN (SELECT numC 
                   FROM LesReservations)
GROUP BY numC, prix ;

Résultat :

NUMC      DUREE   PRIX                                                                                                              
---- ---------- ------                                                                                                              
  17         14   3990                                                                                                              
   6          4   2520                                                                                                              
  16         10  10180                                                                                                              
  15          8   8560                                                                                                              
  11         13   6270

---TESTS---

On récupère les numéros des circuits sans réservation :

SELECT numC
FROM LesCircuits
MINUS
SELECT numC
FROM LesReservations;

NUMC                                                                                                                                
----                                                                                                                                
   6                                                                                                                                
  11                                                                                                                                
  15                                                                                                                                
  16                                                                                                                                
  17

On récupère les prix de ces circuits :

SELECT numC, prix
FROM LesCircuits
WHERE numC IN (6,11,15,16,17);

NUMC   PRIX                                                                                                                         
---- ------                                                                                                                         
   6   2520                                                                                                                         
  11   6270                                                                                                                         
  15   8560                                                                                                                         
  16  10180                                                                                                                         
  17   3990

Et le nombre total de jours de ces circuits :

SELECT numC, SUM(nbJours)
FROM LesEtapes
WHERE numC IN (6,11,15,16,17)
GROUP BY numC;

NUMC SUM(NBJOURS)                                                                                                                   
---- ------------                                                                                                                   
   6            4                                                                                                                   
  11           13                                                                                                                   
  15            8                                                                                                                   
  16           10                                                                                                                   
  17           14

Et on vérifie que tout corresponde bien avec le résultat de notre requête.

QUESTION 8:

---REQUETE : Donner le(s) circuit(s) qui a(ont) le plus grand nombre d'étapes et donner aussi sa(leur) durée---

SELECT numC, SUM(nbJours) AS nbJoursTot
FROM LesEtapes
GROUP BY numC
HAVING COUNT(*) = (
    SELECT MAX(COUNT(*))
    FROM LesEtapes
    GROUP BY numC
);

Résultat :

NUMC NBJOURSTOT                                                                                                                     
---- ----------                                                                                                                     
  19         18                                                                                                                     
  21         22

---TESTS---

On compte le nombre d'étapes pour chaque circuit :

SELECT numC, COUNT(vEtape)
FROM LesEtapes
GROUP BY (numC);

NUMC COUNT(VETAPE)                                                                                                                  
---- -------------                                                                                                                  
   1             1                                                                                                                  
   2             1                                                                                                                  
   3             1                                                                                                                  
   4             1                                                                                                                  
   5             1                                                                                                                  
   6             1                                                                                                                  
   7             5                                                                                                                  
   8             3                                                                                                                  
   9             6                                                                                                                  
  10             7                                                                                                                  
  11             7                                                                                                                  
  12             7                                                                                                                  
  13             8                                                                                                                  
  14             7                                                                                                                  
  15             4 
  16             8                                                                                                                  
  17             9                                                                                                                  
  18             5                                                                                                                  
  19            13                                                                                                                  
  20             1                                                                                                                  
  21            13      

On récupère (manuellement) les numéros des circuits qui ont le plus d'étapes : 19 et 21.
On calcule la durée des circuits 19 et 21 :

SELECT numC, SUM(nbJours)
FROM LesEtapes
WHERE numC IN (19,21)
GROUP BY numC;

NUMC SUM(NBJOURS)                                                                                                                   
---- ------------                                                                                                                   
  19           18                                                                                                                   
  21           22     

On compare avec le résultat de notre requête.                                                                                                             

QUESTION 9:

---REQUETE : Donner le(s) programmations des circuits avec un taux de remplissage de plus de 90% (ex. si le circuit 14 programmé le 26-JUL-10 a 25 places disponibles et on en a réservé 21, il a un taux de remplissage de 84% ). Afficher ce taux de remplissage avec un seul chiffre décimal ainsi que la durée totale du circuit concerné---

WITH RemplissageCircuits AS (
    SELECT numC, dateDep, ROUND(SUM(nbRes)/nbPlaces * 100,1) AS tauxDeRemplissage 
    FROM LesProgrammations JOIN LesReservations USING (numC, dateDep)
    GROUP BY (numC, dateDep, nbPlaces)
)
SELECT numC, dateDep, tauxDeRemplissage, SUM(nbJours) AS nbJoursTotal
FROM RemplissageCircuits JOIN LesEtapes USING (numC)
WHERE tauxDeRemplissage > 90
GROUP BY (numC, dateDep, tauxDeRemplissage);

Résultat :

NUMC DATEDEP                                            TAUXDEREMPLISSAGE NBJOURSTOTAL                                              
---- -------------------------------------------------- ----------------- ------------                                              
  13 31-DEC-09                                                       94.1           18                                              
  20 27-JAN-10                                                        100            6                                              
   7 16-DEC-09                                                       98.1            7                                              
   8 14-FEB-10                                                        100            7                                              
   1 21-JUL-10                                                        100            2                                              
  18 06-DEC-10                                                       91.7           12                                              
   5 06-NOV-10                                                       93.5            5                                              
   2 05-FEB-10                                                        100            2                                              
   3 03-JUL-10                                                       91.7            3       

---TESTS---

On récupère le taux de remplissage pour chaque programmation (circuit + date) :

SELECT numC, dateDep, nbPlaces, SUM(nbRes), ROUND(SUM(nbRes)/nbPlaces * 100,1) AS remplissage
FROM LesProgrammations JOIN LesReservations USING (numC, dateDep)
GROUP BY (numC, dateDep, nbPlaces);

NUMC DATEDEP                                            NBPLACES SUM(NBRES) REMPLISSAGE                                             
---- -------------------------------------------------- -------- ---------- -----------                                             
   4 30-JUN-10                                                99         88        88.9                                             
   8 14-FEB-10                                                 1          1         100                                             
   9 30-OCT-10                                                31         10        32.3                                             
   2 05-FEB-10                                                99         99         100                                             
   5 31-AUG-10                                                66          2           3                                             
  19 15-APR-10                                                45         20        44.4                                             
  18 06-DEC-10                                                12         11        91.7                                             
   2 07-JAN-10                                                34         25        73.5                                             
   5 06-NOV-10                                                46         43        93.5                                             
   8 28-FEB-10                                                11          7        63.6                                             
  14 26-JUL-10                                                25         21          84                                             
  21 15-JAN-10                                                66         24        36.4                                             
   8 16-FEB-10                                                12          1         8.3                                             
  13 01-JAN-10                                                68         10        14.7                                             
  13 31-DEC-09                                                51         48        94.1                                             
  20 27-JAN-10                                                 5          5         100                                             
   1 21-JUL-10                                                10         10         100    
  10 01-JAN-10                                                80          2         2.5                                             
   1 04-FEB-10                                                12          4        33.3                                             
   3 03-JUL-10                                                12         11        91.7                                             
  10 11-FEB-10                                                30          2         6.7                                             
  12 06-FEB-10                                               190         79        41.6                                             
   7 16-DEC-09                                                52         51        98.1  

On garde (manuellement) uniquement celles pour lesquelles le taux de remplissage est supérieur à 90%

NUMC DATEDEP                                            NBPLACES SUM(NBRES) REMPLISSAGE                                             
---- -------------------------------------------------- -------- ---------- -----------                                                                                         
   8 14-FEB-10                                                 1          1         100                                                                                        
   2 05-FEB-10                                                99         99         100                                                                                        
  18 06-DEC-10                                                12         11        91.7                                                                                         
   5 06-NOV-10                                                46         43        93.5                                                                                        
  13 31-DEC-09                                                51         48        94.1                                             
  20 27-JAN-10                                                 5          5         100                                             
   1 21-JUL-10                                                10         10         100                                               
   3 03-JUL-10                                                12         11        91.7                                                                                        
   7 16-DEC-09                                                52         51        98.1 

On calcule pour chaque circuit sa durée totale :

SELECT numC, SUM(nbJours)
FROM LesEtapes
WHERE numC IN (8,2,18,5,13,20,1,3,7)
GROUP BY numC;

NUMC SUM(NBJOURS)                                                                                                                   
---- ------------                                                                                                                   
   1            2                                                                                                                   
   2            2                                                                                                                   
   3            3                                                                                                                   
   5            5                                                                                                                   
   7            7                                                                                                                   
   8            7                                                                                                                   
  13           18                                                                                                                   
  18           12                                                                                                                   
  20            6     

On compare le tout au résultat de notre requête.
