How to create a tab in the Panel:


1. Add your panel to a page from type HeadedPage with the ResultDocModifier. The
first node of your panel must have an id, which identifies the tab.

e.g.
resultDocModifier.addNodes2Elem("tx-panel-detail", <div id="my-tab">My Tab</div>);

2. The id of the first node of your panel can be used as style information for the tab button.
The following snippet adds an icon to the tab button (the size of an icon has to be
22x22):

li.my-tab a {
	background-image:url(/path/images/panel/standard-tab.png);
}


