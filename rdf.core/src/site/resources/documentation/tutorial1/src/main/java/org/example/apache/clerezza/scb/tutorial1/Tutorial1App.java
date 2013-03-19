package org.example.clerezza.scb.tutorial1;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A panel allowing browsing the context of resoures, a main method loads
 * the panel in a new windows.
 * @author rbn
 */

public class Tutorial1App extends JPanel {

    //where our knowledge is stored
    private MGraph mGraph;
    //the URI for which the context is shown
    private String selectedUri;
    //These get notified when the selected URI changes
    private Set<UriChangedListener> uriChangedListeners
            = new HashSet<UriChangedListener>();
    //these get notified when mGraph was modified
    private Set<GraphChangedListener> graphChangedListeners
            = new HashSet<GraphChangedListener>();

    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //Create and set up the window.
                JFrame frame = new JFrame("Triple GUI");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final Tutorial1App tutorial1App = new Tutorial1App("http://dbpedia.org/resource/Category:BBC_television_sitcoms");
                frame.getContentPane().add(tutorial1App);
                //Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    /**
     * Constructs a Tutorial1App with an initially selected URI for which the
     * describing triples are retrieved from the web.
     *
     * @param selectedUri the initial selected URI
     */
    public Tutorial1App(final String selectedUri) {
        this.selectedUri = selectedUri;
        //get the singleton instance of TcManager
        final TcManager tcManager = TcManager.getInstance();
        //the arbitrary name we use for our mutable graph
        final UriRef mGraphName = new UriRef("http://tutorial.example.org/");
        //the m-graph into which we'll put the triples we collect
        mGraph = tcManager.createMGraph(mGraphName);
        try {
            loadContextFromWeb();
        } catch (IOException ex) {
            System.err.println("Error retrieving " + selectedUri);
            ex.printStackTrace();
        }

        Iterator<Triple> typeTriples = mGraph.filter(new UriRef(selectedUri), RDF.type, null);
        while (typeTriples.hasNext()) {
            System.out.println(typeTriples.next());
        }

        setLayout(new BorderLayout());
        add(createNavigation(), BorderLayout.PAGE_START);
        add(createMainArea(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.PAGE_END);
    }

    public String getSelectedUri() {
        return selectedUri;
    }

    public void setSelectedUri(String selectedUri) {
        this.selectedUri = selectedUri;
        for (UriChangedListener uriChangedListener : uriChangedListeners) {
            uriChangedListener.uriChanged();
        }
    }

    public void addGraphChangedListeners(GraphChangedListener gcl) {
        graphChangedListeners.add(gcl);
    }

    public void addUriChangedListeners(UriChangedListener ucl) {
        uriChangedListeners.add(ucl);
    }

    /**
     * 
     * @return the context of the currently selected URI
     */
    public Graph getCurrentContext() {
        return new GraphNode(new UriRef(selectedUri), mGraph).getNodeContext();
    }

    /**
     * Dereference the selected URI and add the retroieved triples to mGraph
     *
     * @throws java.io.IOException
     */
    private void loadContextFromWeb() throws IOException {
        final URL url = new URL(selectedUri);
        final URLConnection con = url.openConnection();
        con.addRequestProperty("Accept", "application/rdf+xml");
        final InputStream inputStream = con.getInputStream();

        //get the singleton instance of Parser
        final Parser parser = Parser.getInstance();
        Graph deserializedGraph = parser.parse(inputStream, "application/rdf+xml");

        mGraph.addAll(deserializedGraph);
        for (GraphChangedListener graphChangedListener : graphChangedListeners) {
            graphChangedListener.graphChanged();
        }
    }

    private JPanel createNavigation() {
        JPanel navigation = new JPanel();
        navigation.add(new JLabel("URI: "));
        final JTextField selectedUriField = new JTextField(selectedUri, 80);
        navigation.add(selectedUriField);
        addUriChangedListeners(new UriChangedListener() {

            @Override
            public void uriChanged() {
                selectedUriField.setText(selectedUri);
            }
        });
        JButton showContextButton = new JButton("Show Context");
        navigation.add(showContextButton);
        JButton loadContextButton = new JButton("Load Context from Web");
        navigation.add(loadContextButton);
        loadContextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setSelectedUri(selectedUriField.getText());
                    loadContextFromWeb();
                    
                } catch (IOException ex) {
                    Logger.getLogger(Tutorial1App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        showContextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setSelectedUri(selectedUriField.getText());
            }
        });
        return navigation;
    }

    private Component createMainArea() {
        //main area with triple-table
        final TripleDataTableModel dataModel = new TripleDataTableModel();
        addUriChangedListeners(dataModel);
        addGraphChangedListeners(dataModel);
        final JTable table = new JTable(dataModel);
        table.setCellSelectionEnabled(true);
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.getSelectedColumn();
                if (col == 1) {
                    return;
                }
                int row = table.getSelectedRow();
                final Object cellValue = dataModel.getValueAt(row, col);
                if (cellValue instanceof UriRef) {
                    setSelectedUri(((UriRef) cellValue).getUnicodeString());
                }
            }
        });
        JScrollPane scrollpane = new JScrollPane(table);
        return scrollpane;
    }

    private Component createFooter() {
        JPanel footer = new JPanel();
        footer.add(new JLabel("Size of local graph: "));
        final JLabel sizeLabel = new JLabel(Integer.toString(mGraph.size()));
        addGraphChangedListeners(new GraphChangedListener() {

            @Override
            public void graphChanged() {
                sizeLabel.setText(Integer.toString(mGraph.size()));
            }
        });
        sizeLabel.setText(Integer.toString(mGraph.size()));
        footer.add(sizeLabel);
        return footer;
    }

    class TripleDataTableModel extends AbstractTableModel implements UriChangedListener, GraphChangedListener {

        private final List<Triple> tripleList = new ArrayList<Triple>();

        public TripleDataTableModel() {
            tripleList.addAll(getCurrentContext());
        }

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return tripleList.size();
        }

        public Object getValueAt(int row, int col) {
            Triple triple = tripleList.get(row);
            switch (col) {
                case 0:
                    return triple.getSubject();
                case 1:
                    return triple.getPredicate();
                default:
                    return triple.getObject();
            }
        }


        @Override
        public void uriChanged() {
            tripleList.clear();
            tripleList.addAll(getCurrentContext());
            fireTableDataChanged();
        }

        @Override
        public void graphChanged() {
            tripleList.clear();
            tripleList.addAll(getCurrentContext());
            fireTableDataChanged();
        }
    }

    public interface UriChangedListener {
        void uriChanged();
    }

    public interface GraphChangedListener {
        void graphChanged();
    }
}
