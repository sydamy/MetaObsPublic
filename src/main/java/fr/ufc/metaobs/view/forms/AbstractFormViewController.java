package fr.ufc.metaobs.view.forms;


import javafx.fxml.FXML;

import java.util.function.Consumer;

/**
 * Classe abstraite pour définir un ViewController pour faire un formulaire, il y a donc une méthode submit et une méthode cancel
 * pour valider ou annuler le formulaire. Le Consumer permet d'indiquer à la classe utilisatrice que le formulaire a été validé ou
 * annulé et ainsi faire les actions appropriées en retour.
 */
public abstract class AbstractFormViewController {

    protected Consumer<Boolean> consumer;

    public Consumer<Boolean> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<Boolean> consumer) {
        this.consumer = consumer;
    }

    /**
     * Méthode à implémenter dans les sous-classes pour vérifier si les données entrées sont suffisantes pour la validation
     * du formulaire.
     *
     * @return un booléen, vrai si les entrées sont suffisantes pour valider, faux sinon
     */
    protected abstract boolean checkRequirements();

    /**
     * Méthode à implémenter dans les sous-classes pour effectuer le traitement nécessaire au formulaire lors de sa validation.
     */
    protected abstract void submitImpl();

    /**
     * Lors d'un appui sur le bouton Submit : appelle la méthode submitImpl puis exécute le Consumer pour indiquer que
     * le formulaire a été validé.
     */
    @FXML
    protected void submit() {
        if (checkRequirements()) {
            submitImpl();
            if (getConsumer() != null) {
                getConsumer().accept(true);
            }
        }
    }

    /**
     * Méthode à implémenter dans les sous-classes pour effectuer le traitement nécessaire au formulaire lors de son annulation.
     */
    protected abstract void cancelImpl();

    /**
     * Lors d'un appui sur le bouton Cancel : appelle la méthode cancelImpl puis exécute le Consumer pour indiquer que
     * le formulaire a été annulé.
     */
    @FXML
    protected void cancel() {
        cancelImpl();
        if (getConsumer() != null) {
            getConsumer().accept(false);
        }
    }

}
