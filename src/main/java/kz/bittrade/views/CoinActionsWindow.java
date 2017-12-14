package kz.bittrade.views;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CoinActionsWindow extends Window {

    public CoinActionsWindow() {
        setContent(new Label("Here's my UI"));
        setClosable(true);
        setDraggable(true);
        setModal(true);
        setResizable(false);
        setCaption("Coin actions");
        setWidth(250.0f, Unit.PIXELS);
        setHeight(250.0f, Unit.PIXELS);
        center();

        VerticalLayout subContent = new VerticalLayout();

        subContent.addComponent(new Label("Yo! Modal window here!"));
        subContent.addComponent(new Button("Run Forest, run!"));

        setContent(subContent);
    }


}