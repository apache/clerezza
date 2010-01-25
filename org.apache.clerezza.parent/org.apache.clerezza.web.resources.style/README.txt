How to create a tab in the Panel:


1. Add your panel tab to a page from type HeadedPage by using the ResultDocModifier. The
first node of your panel tab must have an id, which identifies the tab. A title of
the panel tab could be set by adding a <h3>title</h3> to the first node.

e.g.
resultDocModifier.addNodes2Elem("tx-panel-tabs", <div id="my-tab"><h3>My Tab Title</h3>My Tab</div>);

2. The id of the first node of your panel can be used as style information for
the tab button and specifies the title of the tab button. The following snippet
adds an icon to the tab button (the size of an icon has to be 22x22):

.tx-panel .tx-tab-buttons li.my-tab a {
	background-image:url(/path/images/panel/standard-tab.png);
}
