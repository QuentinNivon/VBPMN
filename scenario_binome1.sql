---------------------------------------------------------------------------------------------------
--QUESTION 1, INUTILE
SELECT DISTINCT nomC
FROM LesReservations;

---------------------------------------------------------------------------------------------------
--QUESTION 2--
----------------------DEMARCHE-----------------------
--On regarde tous les nom apparaissant 1 fois dans la table LesReservations et on compare les noms avec le résultat obtenu:
-----------------------------------------------------

--TEST--
SELECT nomC, COUNT(nomC)
FROM LesReservations
GROUP BY nomC;

--Resultat--
NOMC                                               COUNT(NOMC) 
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


--Les Resultats concordent

----------------------------------------------------------------------------------------------------------------------------------------------
--QUESTION 3--
----------------------DEMARCHE-----------------------
--On regarde tous les numéro des circuits puis tous les numéro des circuit avec la ville de départ et on regarde si notre résultat est le "minus" des 2 table
-----------------------------------------------------

--TEST--
--Tout les numéros de circuit
SELECT numC
FROM LesCircuits

--Resultat
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

--Tout les villes avec leur numéro de parcours (résultat trop grand pour afficher) 
WITH LesParcours AS(
    SELECT numC, vEtape
    FROM LesEtapes
    GROUP BY vEtape, numC
)
--Tout les numéros de circuit passant par la ville de départ
WITH LesParcours AS(
    SELECT numC, vEtape
    FROM LesEtapes
    GROUP BY vEtape, numC
)
SELECT numC
FROM LesCircuits C JOIN LesParcours P USING (numC)
WHERE vDep = vEtape;

--Resultat
NUMC
----
  10 
  14
  17
  19
   7
  20
  12
  
--On voit bien que notre résultat n'est pas parmit les numéros de circuit passant par la ville de départ et est parmis tout les numéros de circuit,
--Notre requête est bonne.

----------------------------------------------------------------------------------------------------------------------------------------------
-- Question 4
----------------------DEMARCHE-----------------------
--On prend tous les circuits qui ont une 2eme étape d'au moins deux jours
-----------------------------------------------------

--TEST--
SELECT numC
FROM LesEtapes
WHERE rang = 2 and nbJours >= 2;

--Resultat
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

10 rows selected.

--Tous les clients qui ont réservé un de ces circuits

SELECT DISTINCT nomC
FROM LesReservations
WHERE numC in (7, 8, 9, 10, 11, 12, 13, 14, 15, 18);

--Resultat
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

11 rows selected.

--On Compare ce tableau au résultat de la requête normale

----------------------------------------------------------------------------------------------------------------------------------------------
--QUESTION 5, INUTILE

----------------------------------------------------------------------------------------------------------------------------------------------
--QUESTION 6
----------------------DEMARCHE-----------------------
--On prend les circuits que Milou va faire
--On prend les villes des circuits visités
--On prend la somme des prix des monuments villes visitées
--On compare donc le résultat avec la table obtenue avec la requete complete
-----------------------------------------------------

--Test
--On prend les circuits que Milou va faire
SELECT numC
FROM LesReservations
WHERE nomC = 'Milou';

-- Resultat
NUMC
----
   8
   2

--On prend les villes des circuits visités
SELECT vEtape
FROM LesEtapes
WHERE numC in (8, 2);

-- Resultat
VETAPE
----------------------------------------
Londres
Laschamps
Orcival
Besse

--On prend la somme des prix des monuments villes visitées

SELECT nomV, SUM(prix)
FROM LesMonuments
WHERE nomV in ('Londres', 'Laschamps', 'Orcival', 'Besse')
GROUP BY nomV;

-- Resultat
NOMV                                      SUM(PRIX)
---------------------------------------- ----------
Londres                                         200

--Seule londre a des monuments, les autres villes devraient être à 0.
--On compare donc le résultat avec la table obtenue avec la requete complete, ça correspond.

----------------------------------------------------------------------------------------------------------------------------------------------
--QUESTION 7
----------------------DEMARCHE-----------------------
--On regarde premièrement les numéro de circuit n'ayant pas de réservations.
--On regarde ensuite le nombre total de jours obtenu pour tout les circuits et on compare à ceux obtenu dans nos résultat
--On regarde ensuite les prix pour tous les circuits et on compare aussi.
-----------------------------------------------------

--TEST--
--Circuit sans réservations
SELECT numC
FROM LesCircuits
MINUS
SELECT numC
FROM LesReservations

--Resultat
NUMC                                                                                                                                
----                                                                                                                                
   6                                                                                                                                
  11                                                                                                                                
  15                                                                                                                                
  16                                                                                                                                
  17  

--Nombre total de jours obtenu
SELECT numC, SUM(nbJours)
FROM LesEtapes
GROUP BY numC;

--Resultat
NUMC SUM(NBJOURS)                                                                                                                   
---- ------------                                                                                                                   
   1            2                                                                                                                   
   2            2                                                                                                                   
   3            3                                                                                                                   
   4            4                                                                                                                   
   5            5                                                                                                                   
   6            4   
   7            7                                                                                                                   
   8            7                                                                                                                   
   9            8                                                                                                                   
  10           13                                                                                                                   
  11           13                                                                                                                   
  12           12                                                                                                                   
  13           18                                                                                                                   
  14           15                                                                                                                   
  15            8   
  16           10                                                                                                                   
  17           14                                                                                                                   
  18           12                                                                                                                   
  19           18                                                                                                                   
  20            6                                                                                                                   
  21           22   

--Prix des circuit
SELECT numC, prix
FROM LesCircuits;

--Resultat
NUMC   PRIX                                                                                                                         
---- ------                                                                                                                         
   1   1160                                                                                                                         
   2   1160                                                                                                                         
   3   1040                                                                                                                         
   4   1270                                                                                                                         
   5   2740                                                                                                                         
   6   2520  
   7   2500                                                                                                                         
   8   2140                                                                                                                         
   9   5700                                                                                                                         
  10   6170                                                                                                                         
  11   6270                                                                                                                         
  12   6170                                                                                                                         
  13  18590                                                                                                                         
  14   7700                                                                                                                         
  15   8560 
  16  10180                                                                                                                         
  17   3990                                                                                                                         
  18   6690                                                                                                                         
  19  10400                                                                                                                         
  21  15000                                                                                                                         
  20    450

--En regroupant ces 3 informations, on en déduit que notre requête est bonne.

----------------------------------------------------------------------------------------------------------------------------------------------
--QUESTION 8
----------------------DEMARCHE-----------------------
--On cherche le nombre d'étapes max
--On cherche les circuits qui ont 13 étapes et le nombre de jours
--Puis on compare avec la table obtenue avec la requete normale.
-----------------------------------------------------

--TEST--
--On cherche le nombre d'étapes max
SELECT MAX(rang)
FROM LesEtapes

-- Resultat
 MAX(RANG)
----------
        13

--On cherche les circuits qui ont 13 étapes et le nombre de jours :
SELECT numC, SUM(nbJours) as nbJoursTot
FROM LesEtapes
GROUP BY numC
HAVING COUNT(vEtape) = 13

-- Resultat
NUMC NBJOURSTOT
---- ----------
  19         18
  21         22

--Puis on compare avec la table obtenue avec la requete normale, c'est la même chose.

----------------------------------------------------------------------------------------------------------------------------------------------
--QUESTION 9
----------------------DEMARCHE-----------------------
-- On regarde les taux de remplissage de tous les trajet
-- On regarde les taux dépassant 90% manuellement
-- On regarde le nombre de jours de ces circuit qui dépassent
-----------------------------------------------------

--TEST--
-- On regarde les taux de remplissage de tous les trajet
SELECT numC, dateDep, ROUND((SUM(nbRes)/nbPlaces)*100,1) AS tauxDeRemplissage
FROM LesProgrammations JOIN LesReservations USING (numC, dateDep)
GROUP BY numC, dateDep, nbPlaces

--Resultat
NUMC DATEDEP                                            TAUXDEREMPLISSAGE 
---- -------------------------------------------------- -----------------                                                           
   4 30-JUN-10                                                       88.9                                                           
   8 14-FEB-10                                                        100                                                           
   9 30-OCT-10                                                       32.3                                                           
   2 05-FEB-10                                                        100                                                           
   5 31-AUG-10                                                          3                                                           
  19 15-APR-10                                                       44.4                                                           
  18 06-DEC-10                                                       91.7                                                           
   2 07-JAN-10                                                       73.5  
   5 06-NOV-10                                                       93.5                                                           
   8 28-FEB-10                                                       63.6                                                           
  14 26-JUL-10                                                         84                                                           
  21 15-JAN-10                                                       36.4                                                           
   8 16-FEB-10                                                        8.3                                                           
  13 01-JAN-10                                                       14.7                                                           
  13 31-DEC-09                                                       94.1                                                           
  20 27-JAN-10                                                        100                                                           
   1 21-JUL-10                                                        100 
  10 01-JAN-10                                                        2.5                                                           
   1 04-FEB-10                                                       33.3                                                           
   3 03-JUL-10                                                       91.7                                                           
  10 11-FEB-10                                                        6.7                                                           
  12 06-FEB-10                                                       41.6                                                           
   7 16-DEC-09                                                       98.1

-- On regarde les taux dépassant 90% manuellement
NUMC DATEDEP                                            TAUXDEREMPLISSAGE                                                           
---- -------------------------------------------------- -----------------                                                           
   8 14-FEB-10                                                        100                                                           
   2 05-FEB-10                                                        100                                                           
  18 06-DEC-10                                                       91.7 
   5 06-NOV-10                                                       93.5                                                           
  13 31-DEC-09                                                       94.1                                                           
  20 27-JAN-10                                                        100                                                           
   1 21-JUL-10                                                        100                                                           
   3 03-JUL-10                                                       91.7                                                           
   7 16-DEC-09                                                       98.1 

-- On regarde le nombre de jours de ces circuit qui dépassent
SELECT numC, SUM(nbJours) AS nbJoursTotal
FROM LesEtapes
WHERE numC IN (1,2,3,5,7,8,13,18,20)
GROUP BY numC;

--Resultat
NUMC NBJOURSTOTAL                                                                                                                   
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

-- On compare maintenant au résultat.
