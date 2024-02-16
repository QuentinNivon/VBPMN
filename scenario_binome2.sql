----------REQUÊTE 1----------

SELECT DISTINCT nomC
FROM LesReservations;

-----Résultat 

/*
NOMC                           
-------------------------------------------------- 
Obelix
Nestor Burma
La Castafiore
Donald
Picsou
Ordralphabetix
Milou
Jerry
Bidochon
Bonnemine
Asterix
Corto
Mafalda
GrosseBaf
*/

-----Scénario 

/*
1) On récupère tous les noms des clients dans la table LesReservations (avec doublons)
*/

SELECT nomC
FROM LesReservations;

/*
Résultats : 

NOMC                           
--------------------------------------------------
Asterix                             
Obelix                               
Donald                               
Picsou                               
Ordralphabetix                               
Corto 
Mafalda                              
Mafalda                              
Donald                             
Bidochon                             
Bonemine                                                         
Nestor Burma                       
Mafalda                              
GrosseBaf                             
Picsou                              
Picsou      
Asterix                             
Milou                             
La Castafiore                           
Milou                              
Jerry    
Donald                             
Ordralphabetix                          
Mafalda                             
Nestor Burma                     
Ordralphabetix  
Nestor Burma

On constate que l'ensemble des clients ayant effectué au moins une réservation est identique entre la requête du scénario 1 et la requête principale, modulo les doublons.
Donc la requête principale est correcte.        
*/

----------REQUÊTE 2----------   

SELECT nomC
FROM LesReservations
GROUP BY nomC
HAVING COUNT(numR) = 1;  

-----Résultat 

/*
NOMC                              
--------------------------------------------------
Obelix                            
La Castafiore                       
Jerry                            
Bidochon                             
Bonemine                          
Corto
GrosseBaf
*/

-----Scénario

/*
1) On récupère la partition des noms des clients selon leur nombre de réservations
*/

SELECT nomC, COUNT(numR)
FROM LesReservations
GROUP BY nomC;

/*
Résultats : 

NOMC                                               COUNT(NUMR)
-------------------------------------------------- -----------
Obelix                                                       1
Nestor Burma                                                 3
La Castafiore                                                1
Donald                                                       3
Picsou                                                       3
Ordralphabetix                                               3
Milou                                                        2
Jerry                                                        1
Bidochon                                                     1
Bonemine                                                     1
Asterix                                                      2
Corto                                                        1
Mafalda                                                      4
GrosseBaf                                                    1    

On constate que les noms des clients ayant effectué exactement une réservation sont Obelix, La Castafiore, Jerry, Bidochon, Bonemine, Corto et GrosseBaf.
Cela correspond aux résultats de la requête principale, qui est donc correcte.
*/


----------REQUÊTE 3----------     

SELECT numC
FROM LesCircuits
MINUS
SELECT numC
FROM LesCircuits JOIN LesEtapes USING (numC)
WHERE vDep = vEtape;

-----Résultat

/*
NUMC
----
   1
   2
   3
   4
   5
   6
   8
   9
  11
  13
  15
  16
  18
  21
*/

-----Scénario

/*
1) On récupère l'ensemble des numéros de circuit
*/

SELECT numC
FROM LesCircuits;

/*
Résultat R1 : 

NUMC                            
----                               
   1                              
   2                             
   3                             
   4                               
   5   
   6                               
   7                                
   8                                
   9                               
  10      
  11                                
  12                              
  13                              
  14                               
  15   
  16                              
  17                             
  18                                
  19                               
  20 
  21
*/

/*
2) On récupère l'ensemble des numéros de circuit pour lesquels on visite la ville de départ
*/

SELECT numC
FROM LesCircuits JOIN LesEtapes USING (numC)
WHERE vDep = vEtape;

/*
Résultat R2 : 

NUMC                            
----                      
   7                               
  10                             
  12                            
  14                               
  17                              
  19   
  20                                     
*/

/*
3) On récupère l'ensemble des numéros de circuit pour lesquels on ne visite pas la ville de départ
*/

SELECT numC
FROM LesCircuits
MINUS
SELECT numC
FROM LesCircuits
WHERE numC = 7 OR numC = 10 OR numC = 12 OR numC = 14 OR numC = 17 OR numC = 19 OR numC = 20;

/*
Résultat R3: 

NUMC                          
----                        
   1                     
   2                            
   3                           
   4                         
   5                          
   6                              
   8                              
   9   
  11                             
  13                             
  15                             
  16                               
  18  
  21

On constate que le résultat R3 correspond au résultat de la requête principale.
Donc la requête principale est correcte.
*/


----------REQUÊTE 4----------      

SELECT DISTINCT nomC
FROM LesReservations JOIN LesEtapes USING (numC)
WHERE rang = 2 AND nbJours >=2;

-----Résultat

/*
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
*/

-----Scénario

/*
1) On récupère tous les numéros de circuit dont la 2eme étape propose un séjour minimum de 2 jours
*/

SELECT numC
FROM LesEtapes
WHERE rang=2 AND nbJours>=2;        

/*
Résultat R1 : 

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
*/

/*
2) On récupère les noms des clients qui ont réservé un de ces circuits 
*/

SELECT DISTINCT nomC
FROM LesReservations 
WHERE numC IN (7,8,9,10,11,12,13,14,15,18);   

/*
Résultat R2 :

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

Le résultat de la requête principale correspond au résultat R2, donc la requête principale est correcte.
*/


----------REQUÊTE 5----------    

SELECT vDep
FROM LesCircuits 
WHERE numC = 1 
UNION
SELECT vEtape
FROM LesEtapes 
WHERE numC = 1 
UNION
SELECT vArr
FROM LesCircuits
WHERE numC = 1;

-----Résultat

/*
VDEP                             
----------------------------------------                            
Londres                            
Paris              
*/

-----Scénario

/*
1) On récupère la ville de départ et d'arrivée du circuit 1
*/

SELECT vDep, vArr
FROM LesCircuits
WHERE numC = 1;      

/*
Résultat R1 :

VDEP                                     VARR
---------------------------------------- ----------------------------------------
Paris                                    Paris
*/

/*
2) On récupère l'ensemble des villes étapes du circuit 1
*/

SELECT vEtape
FROM LesEtapes
WHERE numC = 1;

/*
Résultat R2 : 

VETAPE                          
----------------------------------------                           
Londres   

Donc l'ensemble des villes par lesquelles le circuit 1 passe est {Londres, Paris}.
Cela correspond au résultat de la requête principale, qui est donc correcte.
*/



----------REQUÊTE 6----------        

WITH VillesMilouSansMonument AS (
    SELECT vEtape
    FROM LesEtapes JOIN LesReservations USING (numC)
    WHERE nomC = 'Milou'
    MINUS
    SELECT nomV 
    FROM LesMonuments
)
SELECT vEtape, SUM(prix) as prixTotMon
FROM LesEtapes JOIN LesReservations USING (numC)
               JOIN LesMonuments ON (vEtape = nomV)
WHERE nomC = 'Milou'
GROUP BY vEtape
UNION
SELECT vEtape, 0
FROM VillesMilouSansMonument;

-----Résultat

/*
VETAPE                                   PRIXTOTMON
---------------------------------------- ----------
Besse                                             0
Laschamps                                         0
Londres                                         200
Orcival                                           0
*/ 

-----Scénario

/*
1) On récupère l'ensemble des villes visitées par Milou 
*/

SELECT DISTINCT vEtape
FROM LesReservations JOIN LesEtapes USING (numC)
WHERE nomC = 'Milou';      

/*
Résultat R1 :

VETAPE                        
----------------------------------------                         
Laschamps                          
Besse                             
Londres                           
Orcival
*/

/*
2) On récupère le prix total pour visiter tous les monuments de chaque ville ayant des monuments à visiter et visitée par Milou
*/

SELECT nomV, SUM(prix)
FROM LesMonuments 
GROUP BY nomV
HAVING nomV IN ('Laschamps', 'Besse', 'Londres', 'Orcival');     

/*
Résultat R2 :

NOMV                                      SUM(PRIX)
---------------------------------------- ----------
Londres                                         200
*/

/*
3) On récupère l'ensemble des villes n'ayant aucun monument à visiter et visitées par Milou
*/

SELECT vEtape
FROM LesEtapes JOIN LesReservations USING (numC)
WHERE nomC = 'Milou'
MINUS
SELECT nomV 
FROM LesMonuments;

/*
Résultat R3 :

VETAPE                            
----------------------------------------                          
Besse                         
Laschamps                            
Orcival
*/

/*
4) On récupère le prix total (0) pour visiter les villes n'ayant pas de monuments à visiter et visitées par Milou
*/

SELECT nomV, 0
FROM LesVilles
WHERE nomV IN ('Besse', 'Laschamps', 'Orcival');

/*
Résultat R4 : 

NOMV                                              0
---------------------------------------- ----------
Besse                                             0
Laschamps                                         0
Orcival                                           0
*/

/*
5) On calcule l'union des résultats R2 et R4
*/

SELECT nomV, SUM(prix)
FROM LesMonuments 
GROUP BY nomV
HAVING nomV IN ('Laschamps', 'Besse', 'Londres', 'Orcival')
UNION
SELECT nomV, 0
FROM LesVilles
WHERE nomV IN ('Besse', 'Laschamps', 'Orcival');

/*
Résultat R5 :

NOMV                                      SUM(PRIX)
---------------------------------------- ----------
Besse                                             0
Laschamps                                         0
Londres                                         200
Orcival                                           0

Le résultat R5 correspond au résultat de la requête principale, donc la requête principale est correcte.
*/




----------REQUÊTE 7----------      

WITH CircuitsSansReservation AS (
    SELECT numC
    FROM LesCircuits
    MINUS
    SELECT numC
    FROM LesReservations
), NbTotalJoursParCircuit AS (
    SELECT numC, SUM(nbJours) as nbTotJours
    FROM LesEtapes
    GROUP BY numC
)
SELECT DISTINCT numC, nbTotJours, prix
FROM CircuitsSansReservation JOIN NbTotalJoursParCircuit USING (numC)
                             JOIN LesCircuits USING (numC);

-----Résultat

/*
NUMC NBTOTJOURS   PRIX
---- ---------- ------
  15          8   8560
   6          4   2520
  11         13   6270
  17         14   3990
  16         10  10180
*/

-----Scénario

/*
1) On récupère les numéros des circuits qui n'ont aucune réservation
*/

SELECT numC
FROM LesCircuits
MINUS
SELECT numC
FROM LesReservations;

/*
Résultat R1 : 

NUMC                             
----                             
   6                              
  11                               
  15                              
  16                              
  17
*/

/*
2) On récupère la durée totale en jours de chacun des circuits sans réservation
*/

SELECT numC, SUM(nbJours)
FROM LesEtapes
GROUP BY numC
HAVING numC IN (6, 11, 15, 16, 17);     

/*
Résultat R2 :

NUMC SUM(NBJOURS)
---- ------------
   6            4
  11           13
  15            8
  16           10
  17           14
*/

/*
3) On récupère le prix de base de chacun des circuits sans réservation   
*/

SELECT numC, prix
FROM LesCircuits
WHERE numC IN (6, 11, 15, 16, 17);     

/*
Résultat R3 : 

NUMC   PRIX
---- ------
   6   2520
  11   6270
  15   8560
  16  10180                
  17   3990 

En combinant ces résultats, on doit donc trouver la table suivante : 

NUMC SUM(NBJOURS)   PRIX
---- ------------  -----
   6            4   2520
  11           13   6270
  15            8   8560
  16           10  10180
  17           14   3990      

Cela correspond au résultat de la requête principale, qui est donc correcte.
*/



----------REQUÊTE 8----------

WITH NbEtapesParCircuit AS (
    SELECT numC, COUNT(rang) as NbEtapes
    FROM LesEtapes
    GROUP BY numC
), NbTotalJoursParCircuit AS (
    SELECT numC, SUM(nbJours) as nbTotJours
    FROM LesEtapes
    GROUP BY numC
)
SELECT numC, nbTotJours
FROM NbEtapesParCircuit JOIN NbTotalJoursParCircuit USING (numC)
WHERE nbEtapes IN (SELECT MAX(nbEtapes)
                   FROM NbEtapesParCircuit);

-----Résultat

/*
NUMC NBTOTJOURS
---- ----------
  19         18
  21         22
*/

-----Scénario

/*
1) On construit la partition du nombre d'étapes par circuit
*/

SELECT numC, COUNT(rang)
FROM LesEtapes
GROUP BY numC;         

/*
Résultat R1 :

NUMC COUNT(RANG)                
---- -----------                      
   1           1                   
   2           1                  
   3           1                   
   4           1                 
   5           1      
   6           1                  
   7           5                  
   8           3                   
   9           6                  
  10           7     
  11           7                  
  12           7                  
  13           8                 
  14           7                 
  15           4      
  16           8                 
  17           9                
  18           5                   
  19          13                    
  20           1        
  21          13  

Donc les circuits 19 et 21 ont le plus grand nombre d'étapes.
*/

/*
2) On récupère la durée totale en jour des circuits avec le plus grand nombre d'étapes
*/

SELECT numC, SUM(nbJours)
FROM LesEtapes   
GROUP BY numC
HAVING numC in (19,21);   

/*
Résultat R2 : 

NUMC SUM(NBJOURS)                 
---- ------------                        
  19           18             
  21           22   

Le résultat R2 correspond à celui de la requête principale, donc la requête principale est correcte.
*/



----------REQUÊTE 9----------     

WITH NbPlaces AS (
    SELECT numC, nbPlaces, dateDep
    FROM LesProgrammations
), NbReserv AS (
    SELECT numC, SUM(nbRes) AS nbRes, dateDep
    FROM LesReservations
    GROUP BY numC, dateDep
)
SELECT numC, dateDep, ROUND(100*(nbRes/nbPlaces),1) AS tauxRemplissage, SUM(nbJours)
FROM NbPlaces JOIN NbReserv USING (numC, dateDep) 
              JOIN LesEtapes USING (numC)
GROUP BY numC, dateDep, nbRes, nbPlaces
HAVING (100*(nbRes/nbPlaces)) > 90;

-----Resultat

/*
NUMC DATEDEP                                            TAUXREMPLISSAGE SUM(NBJOURS)
---- -------------------------------------------------- --------------- ------------
   8 14-FEB-10                                                      100            7
   1 21-JUL-10                                                      100            2
   3 03-JUL-10                                                     91.7            3
   2 05-FEB-10                                                      100            2
   7 16-DEC-09                                                     98.1            7
  18 06-DEC-10                                                     91.7           12
  20 27-JAN-10                                                      100            6
   5 06-NOV-10                                                     93.5            5
  13 31-DEC-09                                                     94.1           18    
*/

-----Scénario

/*
1) On récupère la programmation (numéro circuit + date départ) ainsi que le nombre de places de chaque circuit 
*/

SELECT numC, dateDep, nbPlaces
FROM LesProgrammations;       

/*
Résultat R1 :

NUMC DATEDEP                                            NBPLACES
---- -------------------------------------------------- -------
   1 04-JAN-10                                                34
   1 24-JUL-10                                                10
   1 21-JUL-10                                                10
   2 05-SEP-10                                                30
   6 06-SEP-10                                                10  
   7 31-OCT-10                                                39
  10 01-JAN-10                                                80
  11 29-MAY-10                                                34
  18 30-JUL-10                                                90
  21 15-JAN-10                                                66     
   1 04-FEB-10                                                12
   1 06-FEB-10                                                34
   2 05-FEB-10                                                99
   2 06-JAN-10                                                 2
   2 07-JAN-10                                                34  
   2 06-FEB-10                                                12
   3 24-DEC-09                                                13
   3 31-DEC-09                                                45
   4 06-NOV-10                                                18    
   4 06-AUG-10                                                10
   4 30-JUN-10                                                99
   4 31-AUG-10                                                 1
   5 06-NOV-10                                                46
   5 31-AUG-10                                                66      
   6 06-FEB-10                                                22
   6 16-NOV-10                                                34
   6 06-OCT-10                                                12
   7 31-AUG-10                                                56
   7 16-DEC-09                                                52    
   7 06-JAN-10                                                 1
   7 26-FEB-10                                                 1
   8 31-AUG-10                                                10
   8 16-FEB-10                                                12
   8 21-FEB-10                                                13
   8 21-JUL-10                                                14
   8 24-DEC-09                                                18
   8 31-DEC-09                                                10
   8 28-FEB-10                                                11
   8 14-FEB-10                                                 1
   8 16-MAY-10                                                12
   8 26-APR-10                                                31
   8 14-NOV-10                                                 3
   9 06-FEB-10                                                 3
   9 30-OCT-10                                                31  
  10 21-JAN-10                                                45
  10 11-FEB-10                                                30
  10 28-FEB-10                                                22
  11 28-FEB-10                                                 3
  11 06-FEB-10                                                13
  11 30-JUN-10                                                12
  12 06-FEB-10                                               190
  13 31-MAY-10                                                 3
  13 30-APR-10                                                15
  13 30-JUN-10                                                52
  13 31-DEC-10                                                44
  13 31-DEC-09                                                51
  13 01-JAN-10                                                68
  13 06-FEB-10                                                99
  13 06-MAY-10                                                60
  14 06-JUL-10                                                12
  14 26-JUL-10                                                25
  15 06-FEB-10                                                11
  15 31-AUG-10                                                18
  16 31-AUG-10                                                17
  16 06-FEB-10                                                12
  16 06-JAN-10                                                 3
  17 31-DEC-10                                                 3
  17 06-FEB-10                                                 3
  17 16-FEB-10                                                12
  17 26-FEB-10                                                34
  18 30-APR-10                                                15
  18 06-DEC-10                                                12
  18 06-OCT-10                                                40
  19 06-SEP-10                                                20
  19 16-SEP-10                                                10
  19 05-AUG-10                                                12
  19 10-DEC-10                                                28
  19 20-DEC-10                                                11
  19 06-FEB-10                                                10
  19 15-APR-10                                                45
  20 01-APR-10                                                13
  20 02-FEB-10                                                14
  20 22-DEC-09                                                18
  20 20-JAN-10                                                12
  20 27-JAN-10                                                 5
  20 03-APR-10                                                87
  20 06-FEB-10                                                 1
  21 26-FEB-10                                                12
  21 06-APR-10                                                99
  21 25-JAN-10                                                 3   
*/

/*
2) On récupère la programmation (numéro circuit + date départ) ainsi que le nombre total de réservations de chaque circuit
*/

SELECT numC, dateDep, SUM(nbRes) AS nbRes
FROM LesReservations
GROUP BY numC, dateDep;

/*
Résultat R2 : 

NUMC DATEDEP                                            NBRES
---- -------------------------------------------------- -----
   8 28-FEB-10                                              7
   3 03-JUL-10                                             11
   7 16-DEC-09                                             51
  13 01-JAN-10                                             10
  12 06-FEB-10                                             79
  18 06-DEC-10                                             11
   8 14-FEB-10                                              1
   8 16-FEB-10                                              1
   4 30-JUN-10                                             88
   5 31-AUG-10                                              2
   2 07-JAN-10                                             25
  19 15-APR-10                                             20
   5 06-NOV-10                                             43
  21 15-JAN-10                                             24
  13 31-DEC-09                                             48
  14 26-JUL-10                                             21
  20 27-JAN-10                                              5
   1 21-JUL-10                                             10
   2 05-FEB-10                                             99
   1 04-FEB-10                                              4
  10 01-JAN-10                                              2
   9 30-OCT-10                                             10
  10 11-FEB-10                                              2
*/

/*
3) On calcule le taux de remplissage (nbRes / nbPlaces) ainsi que la durée totale en jours de chaque circuit pour lequel il existe au moins une réservation (taux non nul)
*/

WITH NbPlaces AS (
    SELECT numC, nbPlaces, dateDep
    FROM LesProgrammations
), NbReserv AS (
    SELECT numC, SUM(nbRes) AS nbRes, dateDep
    FROM LesReservations
    GROUP BY numC, dateDep
)
SELECT numC, dateDep, ROUND(100*(nbRes/nbPlaces),1) AS tauxRemplissage, SUM(nbJours)
FROM NbReserv JOIN NbPlaces USING (numC, dateDep)
	      JOIN LesEtapes USING (numC)
GROUP BY numC, dateDep, nbRes, nbPlaces;

/*
Résultat R3 : 

NUMC DATEDEP                                            TAUXREMPLISSAGE SUM(NBJOURS)                                                
---- -------------------------------------------------- --------------- ------------                                                
   5 31-AUG-10                                                        3            5                                                
   8 14-FEB-10                                                      100            7                                                
   9 30-OCT-10                                                     32.3            8                                                
  21 15-JAN-10                                                     36.4           22                                                
   1 21-JUL-10                                                      100            2                                                
   3 03-JUL-10                                                     91.7            3                                                
  12 06-FEB-10                                                     41.6           12
   2 07-JAN-10                                                     73.5            2                                                
   4 30-JUN-10                                                     88.9            4                                                
   8 28-FEB-10                                                     63.6            7                                                
  10 11-FEB-10                                                      6.7           13                                                
   2 05-FEB-10                                                      100            2        
  13 01-JAN-10                                                     14.7           18                                                
  14 26-JUL-10                                                       84           15                                                
  10 01-JAN-10                                                      2.5           13                                                
   7 16-DEC-09                                                     98.1            7                                                
  18 06-DEC-10                                                     91.7           12   
  19 15-APR-10                                                     44.4           18                                                
  20 27-JAN-10                                                      100            6                                                
   1 04-FEB-10                                                     33.3            2                                                
   5 06-NOV-10                                                     93.5            5                                                
   8 16-FEB-10                                                      8.3            7     
  13 31-DEC-09                                                     94.1           18 

Les seuls circuits (couples numC, dateDep) avec un taux de remplissage supérieur à 90% sont :
(8, 14-FEB-10)  avec 100%  de remplissage et une durée de 7 jours
(1, 21-JUL-10)  avec 100%  de remplissage et une durée de 2 jours
(3, 03-JUL-10)  avec 91.7% de remplissage et une durée de 3 jours
(2, 05-FEB-10)  avec 100%  de remplissage et une durée de 2 jours
(7, 16-DEC-09)  avec 98.1% de remplissage et une durée de 7 jours
(18, 06-DEC-10) avec 91.7% de remplissage et une durée de 12 jours
(20, 27-JAN-10) avec 100%  de remplissage et une durée de 6 jours
(5, 06-NOV-10)  avec 93.5% de remplissage et une durée de 5 jours
(13, 31-DEC-09) avec 94.1% de remplissage et une durée de 18 jours 

Cela correspond à la table obtenue par la requête principale : 

NUMC DATEDEP                                            TAUXREMPLISSAGE SUM(NBJOURS)                                                
---- -------------------------------------------------- --------------- ------------                                                
   8 14-FEB-10                                                      100            7                                                
   1 21-JUL-10                                                      100            2                                                
   3 03-JUL-10                                                     91.7            3                                                
   2 05-FEB-10                                                      100            2                                                
   7 16-DEC-09                                                     98.1            7                                                
  18 06-DEC-10                                                     91.7           12                                                
  20 27-JAN-10                                                      100            6                                                
   5 06-NOV-10                                                     93.5            5                                                
  13 31-DEC-09                                                     94.1           18       

Donc la requête principale est correcte.  
*/                                                           