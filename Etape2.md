# Éléments utils à l'Étape 2

## Accès aux caractèristiques du hardware

Le hardware a des interfaces `{nom du composant}StaticStateI` et `{nom du composant}DynamiqueStateI` qui permettent de recupérer les caractèristiques de certains composant.

### Élements disponibles :
####  Computer

`fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamiqueStateI`
+ Réservation des cores.

`fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI`
+ Liste des cores.
+ Liste des CPUs.

### Processor

`fr.upmc.datacenter.hardware.processors.interfaces.ComputerStaticStateI`
+ Idle status des cores.
+ Fréquence actuelle des cores.

`fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStaticStateI`
+ Tout ce qui concerne la fréquence du CPU (min/max/paliers).
+ Puissance de calcule.


### Outils pour manager les perfomances

+ Add/rm AVMs.
+ Changer la fréquence des coeurs.
+ Allouer/désallouer des cores aux AVMs.

Travail à éffectuer notament sur ComputerPool et AdmissionController.

### Instrumentation des RequestDispatchers

Prévoir un moyen de réaliser des moyennes de temps de traitement des requêtes sans être trop "invasif" (ne pas trop diminuer les perfomances en contrôlant trop les temps de calcules).

### Politiques de régulation

Prévoir des controleurs muni de politiques prédefinies (strategy pattern).

#### Éxemple
Régulation à deux seuils:
+ Trigger lors d'un premier seuil.
+ Une fois le premier seuil passé, le controleur doit passer un autre seuil different du premier pour lancer le un régulation inverse à la premiere.
+ Définition des seuils en dur.