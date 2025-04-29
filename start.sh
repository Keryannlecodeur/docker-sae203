#!/bin/bash

echo "Choisissez un mode :"
echo "1 - Serveur"
echo "2 - Client"
read -p "Entrer 1 ou 2 : " choix

if [ "$choix" = "1" ]; then
    echo "Lancement du serveur..."
    java ServeurPuissance4
elif [ "$choix" = "2" ]; then
    echo "Lancement du client..."
    java ClientPuissance4
else
    echo "Choix invalide."
    exit 1
fi
