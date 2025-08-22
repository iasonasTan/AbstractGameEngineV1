package com.engine;

import com.engine.entity.AbstractEntity;
import com.engine.entity.DefaultEntityManager;
import com.engine.entity.Entity;
import com.engine.map.AbstractTile;
import com.engine.map.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Saves the exact state of the game in a xml file. All entities and tiles must provide their implementation
 * of {@link AbstractEntity#createXmlElement(Element)} and {@link AbstractEntity#loadFromXmlElement(Element)}.
 * {@code Entity} and {@code EntityManager} IDs are also saved and restored.
 * @see AbstractEntity#createXmlElement(Element)
 * @see AbstractEntity#loadFromXmlElement(Element)
 */
public final class ManifestManager {
    /**
     * Xml file contains game manifest.
     */
    private final File mManifestFile;

    /**
     * Game to load from or store to xml.
     */
    private final AbstractGame mContext;

    /**
     * Name of root tag of manifest.
     */
    private final String mRootTagName ="manifest";

    /**
     * Name of tag containing entities of the context.
     */
    private final String mEntitiesElementName ="entities";

    /**
     * Name of tag containing entities of entity managers of the context.
     */
    private final String mEntitiesManagersElementName ="entityManagers";

    /**
     * Name of tag containing tiles of map handler of context.
     */
    private final String mTilesElementName="tiles";

    /**
     * Name of tag representing entity manager.
     */
    private final String mEntityManagerElementName="entityManager";

    /**
     * Constructor taking manifest file and game.
     * @see #mContext
     * @see #mManifestFile
     * @param manifestFile file contains game manifest.
     * @param context game to load from or store to xml.
     */
    public ManifestManager(File manifestFile, AbstractGame context) {
        mManifestFile=manifestFile;
        mContext=context;
        System.out.println("[DEBUG] New manifest manager created "+this);
    }

    /**
     * {@return a string representation of the object}
     * <p>
     * Satisfying this method's contract implies a non-{@code null}
     * result must be returned.
     *
     * @apiNote In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * The string output is not necessarily stable over time or across
     * JVM invocations.
     * @implSpec The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * {@snippet lang = java:
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     *}
     * The {@link Objects#toIdentityString(Object)
     * Objects.toIdentityString} method returns the string for an
     * object equal to the string that would be returned if neither
     * the {@code toString} nor {@code hashCode} methods were
     * overridden by the object's class.
     */
    @Override
    public String toString() {
        return getClass().getName()+":I/O File: "+mManifestFile+", I/O Context:"+mContext;
    }

    /**
     * Loads game entities, entity managers, sprites from xml file.
     * @throws ParserConfigurationException When a serious configuration error occurs.
     * @throws IOException Something went wrong while parsing the xml document.
     * @throws SAXException When SAX error occurs.
     */
    public void loadGameFromXml() throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException {
        // load file to objects
        DocumentBuilder documentBuilder=DocumentBuilderFactory.newNSInstance().newDocumentBuilder();
        Document document=documentBuilder.parse(mManifestFile);

        // get root element
        Element element= document.getDocumentElement();

        // load game
        loadTiles(element);
        loadContextEntities(element);
        loadEntityManagers(element);
    }

    /**
     * Loads all entity managers from xml to memory.
     * @param rootElement root element of document.
     * @throws ClassNotFoundException when a defined class could not be found in project.
     */
    private void loadEntityManagers(Element rootElement) throws ClassNotFoundException {
        Element entityManagersElement=(Element)rootElement.getElementsByTagName(mEntitiesManagersElementName).item(0); // get the only element that contains entity managers
        NodeList entityManagersElementsList=entityManagersElement.getChildNodes(); // get entity manager elements
        for (int i = 0; i < entityManagersElementsList.getLength(); i++) { // iterate through all entity manager elements
            if(entityManagersElementsList.item(i) instanceof Element entityManagerElement) {
                DefaultEntityManager<AbstractEntity> entityManager=new DefaultEntityManager<>(mContext); // create entity manager
                NodeList entityElementsList=entityManagerElement.getChildNodes(); // get entities of entity manager
                for (int j = 0; j < entityElementsList.getLength(); j++) { // iterate through all entities of entity manager
                    if(entityElementsList.item(j) instanceof Element entityElement) {
                        AbstractEntity entity=getEntityImplementation(entityElement.getTagName()); // create entity manager of X type
                        entity.loadFromXmlElement(entityElement); // load properties to entity
                        entityManager.add(entity); // add entity to entity manager
                        entity.startRendering(); // allow entity to draw
                    }
                }
                mContext.addEntityManager(entityManagerElement.getAttribute("id"), entityManager); // add entity manager to context
            }
        }
    }

    /**
     * Loads all entities from xml to context.
     * @param rootElement root element of document.
     * @throws ClassNotFoundException when a defined class could not be found in project.
     */
    private void loadContextEntities(Element rootElement) throws ClassNotFoundException {
        Element entitiesElement= (Element) rootElement.getElementsByTagName(mEntitiesElementName).item(0); // gets the only one element that contains entities
        NodeList entitiesList=entitiesElement.getChildNodes(); // creates a list containing entity elements
        for (int i = 0; i < entitiesList.getLength(); i++) { // iterates through all entity elements
            if(entitiesList.item(i) instanceof Element entityElement) {
                String typeText = entityElement.getTagName(); // get entity type as text
                AbstractEntity entity=getEntityImplementation(typeText); // create entity with this name
                entity.loadFromXmlElement(entityElement); // gives entity the xml element to load some properties
                mContext.addEntity(entityElement.getAttribute("id"), entity); // adds entity to context with its ID
                entity.startRendering(); // allows entity to draw itself
            }
        }
    }

    /**
     * Loads tiles from xml document to game.
     * @param rootElement root element of manifest.
     * @throws ClassNotFoundException when a defined class could not be found in project.
     */
    private void loadTiles(Element rootElement) throws ClassNotFoundException {
        Element tiles = (Element) rootElement.getElementsByTagName(mTilesElementName).item(0); // gets the only one element containing tiles
        NodeList tilesList=tiles.getChildNodes(); // creates a list containing tile elements
        for (int i = 0; i < tilesList.getLength(); i++) { // iterate through all tile elements
            if(tilesList.item(i) instanceof Element tileElement) {
                String typeText = tileElement.getTagName(); // get type of tile as text
                Entity tileEntity = getEntityImplementation(typeText); // create entity
                if (tileEntity instanceof AbstractTile tile) { // check if entity is instance of tile
                    tile.loadFromXmlElement(tileElement); // give element to entity so it can load its properties
                    tile.startRendering(); // allow tile to draw
                    mContext.getMap(Map.class).add(tile); // add tile to map
                } else { // if not
                    throw new RuntimeException("Class " + tileEntity.getClass() + " is not a tile."); // throw exception
                }
            }
        }
    }

    /**
     * <p>
     * Returns an instance of the given class.
     * Given class <i>must</i> extend {@link AbstractEntity} <i>and</i>> declare a constructor taking ({@link AbstractGame})
     * </p>
     * <b>WARNING</b> Do not use simple name of class, e.g. String; instead use full name java.lang.String
     * @param className name of class to make instance of
     * @return returns instance of class.
     * @throws ClassNotFoundException if class with given name cannot be found.
     */
    private AbstractEntity getEntityImplementation(String className) throws ClassNotFoundException {
        try {
            Class<?> rawClass = Class.forName(className); // get class from given name
            if (!AbstractEntity.class.isAssignableFrom(rawClass)) // if class from given name is not assignable from AbstractEntity class
                throw new IllegalArgumentException("Class "+className+" is not a subtype of AbstractEntity."); // throw IllegalArgumentException
            // noinspection all
            Class<? extends AbstractEntity> tileClass = (Class<? extends AbstractEntity>) rawClass; // convert class to abstract entity subclass
            return tileClass.getDeclaredConstructor(AbstractGame.class).newInstance(mContext); // create and return instance of class
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Subclass of AbstractEntity must declare a constructor taking args (AbstractGame)");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate tile: " + className, e);
        }
    }


    /**
     * Stores game context to xml manifest.
     * @throws ParserConfigurationException when a serious configuration error occurs.
     */
    public void storeGameToXml() throws ParserConfigurationException, TransformerException {
        // create document
        DocumentBuilder documentBuilder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document=documentBuilder.newDocument();

        // create root tag
        Element rootElement=document.createElement(mRootTagName);

        // create tags for entities.
        storeEntities(rootElement, document);
        storeEntityManagers(rootElement, document);
        storeTiles(rootElement, document);

        document.appendChild(rootElement);

        // save elements to file
        Transformer transformer= TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(mManifestFile));
    }

    /**
     * Stores tiles of map handler.
     * @param rootElement root element to append child tags to.
     * @param document document used to create elements.
     */
    private void storeTiles(Element rootElement, Document document) {
        Element tilesElement=document.createElement(mTilesElementName); // create tiles element
        mContext.getMap(Map.class).forEach(tile -> { // iterate through all tiles in context
            if(tile instanceof AbstractTile abstractTile) {
                Element tileElement=document.createElement(tile.getClass().getName()); // create an element with tile's class name
                abstractTile.createXmlElement(tileElement); // give element to tile so it sores it's information in it
                tilesElement.appendChild(tileElement); // append tile element to tiles element
            } else {
                System.out.println("[DEBUG] Failed to store tile "+tile+", this is not subclass of "+AbstractTile.class.getName());
            }
        });
        rootElement.appendChild(tilesElement); // add tiles element to root element
    }

    /**
     * Stores entity managers and their IDs to xml manifest.
     * @param rootElement root element to append child tags to.
     * @param document document used to create elements.
     */
    private void storeEntityManagers(Element rootElement, Document document) {
        Element entityManagersElement=document.createElement(mEntitiesManagersElementName); // create an element that will contain entity manager
        mContext.getEntityManagersMap().forEach((id, manager) -> { // iterate through all entity managers
            Element managerElement=document.createElement(mEntityManagerElementName); // create element represent an entity manager
            managerElement.setAttribute("id", id); // store entity manager's ID to tag
            manager.forEach(entity -> { // iterate through all entities of entity manager
                if(entity instanceof AbstractEntity abstractEntity) {
                    Element entityElement=document.createElement(entity.getClass().getName()); // create an element for each one
                    abstractEntity.createXmlElement(entityElement); // let entity store its information to this element
                    managerElement.appendChild(entityElement); // append entity element to manager element
                } else {
                    System.out.println("[DEBUG] Failed to store "+entity+", this is not subclass of "+AbstractEntity.class.getName());
                }
            });
            entityManagersElement.appendChild(managerElement); // append manager element to managers element
        });
        rootElement.appendChild(entityManagersElement); // append managers element to root element
    }

    /**
     * Stores entities of context to xml file.
     * @param rootElement root element to append child tags to.
     * @param document document used to create elements.
     */
    private void storeEntities(final Element rootElement, final Document document) {
        Element entitiesElement=document.createElement(mEntitiesElementName); // create tag containing entities
        mContext.getEntitiesMap().forEach((id, entity) -> { // iterate through all entities
            if(entity instanceof AbstractEntity abstractEntity) {
                Element entityElement=document.createElement(entity.getClass().getName()); // create entity element
                entityElement.setAttribute("id", id); // store entity's ID
                abstractEntity.createXmlElement(entityElement); // give element to entity so entity stores what it wants
                entitiesElement.appendChild(entityElement); // append entity element to entities element
            } else {
                System.out.println("[DEBUG] Failed to store "+entity+", this is not a subclass of "+AbstractEntity.class.getName());
            }
        });
        rootElement.appendChild(entitiesElement); // append entities element to root element
    }
}
