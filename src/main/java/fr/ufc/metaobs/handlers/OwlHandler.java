package fr.ufc.metaobs.handlers;

import fr.ufc.metaobs.enums.FieldsTypes;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;

/**
 * contain functions to manipulate owl files
 */
public class OwlHandler {

    private OntModel model;    //contains base and all referenced ontologies
    private OntModel baseModel;
    private String metaobsURI;
    private List<OntResource> listImports;
    private Map<OntClass, OntClass> listEquivalentClasses;

    // Constructor
    public OwlHandler() {
    }

    // getters and setters
    public String getMetaobsURI() {
        return metaobsURI;
    }

    public void setMetaobsURI(String metaobsURI) {
        this.metaobsURI = metaobsURI;
    }

    public List<String> getListImportsUris() {
        List<String> uris = new ArrayList<>();
        for (OntResource r : listImports) {
            uris.add(r.getURI());
        }
        return uris;
    }

    /**
     * initialization OntModel -- OntModel wraps an OWL inferencing model, We
     * use it to operate the ontology
     */
    public void init(String metaobs, OntModel projectModel) {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        baseModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model.getBaseModel());

        //read metaobs
        model.read(metaobs, "RDF/XML");

        //on ajoute le modèle du projet au model owl pour faire l'union
        model.add(projectModel);

        //get URI of metaobs
        getImports();
        getEquivalentClasses();
    }

    /**
     * get URI of metaobs; get all imported ontologies
     */
    private void getImports() {
        for (Iterator<Ontology> iterator = baseModel.listOntologies(); iterator.hasNext(); ) {
            Ontology baseOnto = iterator.next();
            metaobsURI = baseOnto.getURI();
            listImports = baseOnto.listImports().toList();
        }
    }

    /**
     * get all the equivalent classes
     */
    private void getEquivalentClasses() {
        listEquivalentClasses = new HashMap<>();

        List<OntClass> allClasses = model.listClasses().toList();
        for (OntClass c : allClasses) {
            OntClass equivC = c.getEquivalentClass();
            if (equivC != null) {
                listEquivalentClasses.put(c, equivC);
                listEquivalentClasses.put(equivC, c);
            }
        }
    }

    /**
     * get all the classes that are annotated with the given tag
     *
     * @param tag
     * @return list of classes that are annotated with the given tag
     */
    public List<OntClass> getClassesWithTag(String tag) {
        List<OntClass> list = new ArrayList<>();
        List<OntClass> allClasses = model.listClasses().toList();

        try {
            for (OntClass c : allClasses) {
                Iterator<OntClass> i = c.listSuperClasses();
                while (i.hasNext()) {
                    OntClass superClass = i.next();
                    if (superClass.isRestriction()) {
                        Restriction r = superClass.asRestriction();
                        if (r.isSomeValuesFromRestriction()) {
                            SomeValuesFromRestriction svr = r.asSomeValuesFromRestriction();
                            if (getLocalName(svr.getSomeValuesFrom()).equals(tag)) {
                                list.add(c);
                            }
                        }
                    }
                }
            }
        } catch (ConversionException ignored) {
            //on n'affiche pas la StackTrace
        }
        return list;
    }

    public OntClass getClassFromURI(String uri) {
        return model.getOntClass(uri);
    }

    public int getMetadataOrder(OntClass ontClass) {
        int order = 0;
        for (Iterator<OntClass> i = ontClass.listSuperClasses(); i.hasNext(); ) {
            OntClass superC = i.next();
            if (superC.isRestriction()) {
                Restriction r = superC.asRestriction();
                if (r.isHasValueRestriction()) {
                    HasValueRestriction hvr = r.asHasValueRestriction();
                    Object value = hvr.getHasValue();
                    if (value instanceof LiteralImpl) {
                        try {
                            order = ((LiteralImpl) value).getInt();
                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return order;
    }

    /**
     * get the informations to dynamically create the general meta-data form
     *
     * @param obs        - input parameter : classes that are tagged general meta-data
     * @param fieldTypes - output parameter
     * @param orders     - output parameter
     * @param labels     - output parameter
     */
    public void getObsInfos(List<OntClass> obs, Map<OntClass, String> fieldTypes, SortedMap<Integer, OntClass> orders, Map<OntClass, String> labels) {
        for (OntClass c : obs) {
            for (Iterator<OntClass> i = c.listSuperClasses(); i.hasNext(); ) {
                OntClass superC = i.next();
                if (superC.isRestriction()) {
                    Restriction r = superC.asRestriction();
                    if (r.isHasValueRestriction()) {
                        HasValueRestriction hvr = r.asHasValueRestriction();
                        Object value = hvr.getHasValue();
                        if (value instanceof LiteralImpl) {
                            try {
                                Integer order = ((LiteralImpl) value).getInt();
                                orders.put(order, c);
                            } catch (NumberFormatException e) {
                                String label = ((LiteralImpl) value).getString();
                                labels.put(c, label);
                            }
                        }
                    } else if (r.isAllValuesFromRestriction()) {
                        AllValuesFromRestriction avf = r.asAllValuesFromRestriction();
                        String type = getLocalName(avf.getAllValuesFrom());
                        if (type.equals(FieldsTypes.TextField.toString())
                                || type.equals(FieldsTypes.List.toString())
                                || type.equals(FieldsTypes.TextArea.toString())
                                || type.equals(FieldsTypes.Enumeration.toString())
                                || type.equals(FieldsTypes.SpatialCoverage.toString())
                                || type.equals(FieldsTypes.CreateDate.toString())) {
                            fieldTypes.put(c, type);
                        }
                    }
                }
            }
        }
    }

    /**
     * get the local name of the resource in the ontology (URI of resource = URI
     * of ontology + "#" + local name)
     *
     * @param resource
     * @return local name of the resource
     */
    public String getLocalName(Resource resource) {
        if (resource.getProperty(RDFS.label) != null) {
            return resource.getProperty(RDFS.label).getString();
        }
        String URI = resource.getURI();
        return URI.substring(URI.indexOf('#') + 1);
    }

    /**
     * get all classes that has a simple relationship with the given class
     *
     * @return all classes that has a simple relationship with the given class
     */
    public List<OntResource> getRelationSimple(OntClass target) {

        List<OntResource> simpleRelation = new ArrayList<>();

        for (ExtendedIterator<OntClass> i = target.listSuperClasses();
             i.hasNext(); ) {
            OntClass c = i.next();
            if (c.isRestriction()) {
                Restriction restriction = c.asRestriction();
                if (restriction.isCardinalityRestriction()) {
                    CardinalityRestriction exactly = restriction.asCardinalityRestriction();
                    OntResource range = exactly.getOnProperty().getRange();
                    simpleRelation.add(range);
                }
            }
        }
        return simpleRelation;
    }

    /**
     * get all the subclasses of a class or its equivalent class
     *
     * @param superclass
     * @return list of subclasses
     */
    public List<OntClass> getSubclasses(OntClass superclass) {
        List<OntClass> list = superclass.listSubClasses().toList();

        if (list.size() == 0) {
            for (Map.Entry<OntClass, OntClass> entry : listEquivalentClasses.entrySet()) {
                if ((superclass.getURI()).equals(entry.getKey().getURI())) {
                    list = entry.getValue().listSubClasses().toList();
                    break;
                }
            }
        }
        return list;
    }

    /**
     * get all the subclasses names of a class or its equivalent class
     *
     * @param superclass
     * @return list of subclasses names
     */
    public List<String> getSubclassesNames(OntClass superclass) {
        List<String> map = new ArrayList<>();
        List<OntClass> list = getSubclasses(superclass);
        for (OntClass c : list) {
            map.add(getLocalName(c));
        }

        return map;
    }

    /**
     * get all the subclasses of a class (including subclasses of subclasses)
     *
     * @param superclass
     * @param map
     */
    public void getSubclassesTrans(OntClass superclass, Map<String, OntClass> map) {
        List<OntClass> list = getSubclasses(superclass);
        for (OntClass c : list) {
            map.put(getLocalName(c), c);
            getSubclassesTrans(c, map);
        }
    }

    /**
     * get the subclass levels for a class
     *
     * @param superClass
     * @return number of levels
     */
    public int getSubclassLayers(OntClass superClass) {
        int cnt = 0;
        List<OntClass> list = getSubclasses(superClass);

        if (!list.isEmpty()) {
            cnt++;
        } else {
            return 0;
        }

        int max = 0;
        for (OntClass c : list) {
            int tmp = getSubclassLayers(c);
            max = Math.max(tmp, max);
        }

        return cnt + max;
    }

}
