package no.seapop.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import no.npolar.util.CmsAgent;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;

/**
 * Holds a list of {@link SpeciesData} instances.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class SpeciesDataCollection {
    // All locations
    private List<String> locations = null;
    // All bird species names, as Strings
    private List<String> names = null;
    // All data entries (one per bird species)
    private List<SpeciesData> data = null;
    
    
    /*public SpeciesDataCollection() {
        locations = new ArrayList<String>();
        names = new ArrayList<String>();
        data = new ArrayList<SpeciesData>();
    }*/
    
    /**
     * Creates a new collection by constructing data entries based on the 
     * <code>seapop_species_data</code> data files found in the given folder.
     * <p>
     * After the collection has been created, it is sorted by group.
     * 
     * @param folder The folder to read <code>seapop_species_data</code> data files from.
     * @param cms Initialized CmsAgent.
     */
    public SpeciesDataCollection(String folder, CmsAgent cms) {
        //this();
        locations = new ArrayList<String>();
        names = new ArrayList<String>();
        data = new ArrayList<SpeciesData>();
        try {
            CmsObject cmso = cms.getCmsObject();
            CmsResourceFilter dataFilesFilter = CmsResourceFilter.DEFAULT_FILES.addRequireType(OpenCms.getResourceManager().getResourceType(SpeciesData.RESOURCE_TYPE_NAME).getTypeId());

            // Load data files
            List<CmsResource> ocmsDataFiles = cmso.readResources(folder, dataFilesFilter, false);
            Iterator<CmsResource> iOcmsDataFiles = ocmsDataFiles.iterator();
            while (iOcmsDataFiles.hasNext()) {
                SpeciesData speciesDataEntry = new SpeciesData(cmso.getSitePath(iOcmsDataFiles.next()), cms);
                this.add(speciesDataEntry);
                
            }            
        } catch (Exception e) {
            // ???
        }
        
        sortByGroup();
    }
    
    /**
     * @see #toHtmlTableRows(no.npolar.util.CmsAgent, java.lang.String, java.util.List) 
     */
    public String toHtmlTableRows(CmsAgent cms) {
        return toHtmlTableRows(cms, null);
    }
    /**
     * @see #toHtmlTableRows(no.npolar.util.CmsAgent, java.lang.String, java.util.List) 
     */
    public String toHtmlTableRows(CmsAgent cms, String dataType) {
        return toHtmlTableRows(cms, dataType, SpeciesDataLinkType.TYPES_ORDER_DEFAULT);
    }
    /**
     * @see #toHtmlTableRows(no.npolar.util.CmsAgent, java.lang.String, java.util.List) 
     */
    public String toHtmlTableRows(CmsAgent cms, String dataType, List<String> dataTypeTotals) {
        return toHtmlTableRows(cms, dataType, dataTypeTotals, null);
    }
    
    /**
     * Generates HTML table rows based on the data in this collection, and the 
     * given "type totals" (aggregated sums per data type).
     * 
     * @param cms
     * @param dataType If provided, the generated table will include only this data type. If null, all data types will be included.
     * @param dataTypeTotals The names of the data types to aggregate data for, and their order.
     * @param excludedDataTypes The names of the data types to exclude from the table.
     * @return 
     */
    public String toHtmlTableRows(CmsAgent cms, String dataType, List<String> dataTypeTotals, List<String> excludedDataTypes) {
        if (excludedDataTypes == null) {
            excludedDataTypes = new ArrayList<String>(); // Prevent NPE
        }
        
        List<String> comments = new ArrayList<String>();
        String s = "";
        
        Iterator<String> iLocations = locations.iterator();
        while (iLocations.hasNext()) {
            // Get the location
            String location = iLocations.next();
            
            // Construct the array of aggregated sums per data type + 1 for the combined total
            int[] sums = new int[dataTypeTotals.size()+1]; // E.g.: [Population] [Reproduction] [Survival] [Diet] [COMBINED TOTAL (always last)]
            for (int sum : sums) {
                sum = 0;
            }
            
            // Start the row, using the location as the row header
            s += "<tr><th scope=\"row\">" + location + "</th>";
            
            // Now iterate the data entries ...
            Iterator<SpeciesData> iData= data.iterator();
            while (iData.hasNext()) {
                // A data entry
                SpeciesData speciesDataEntry = iData.next();
                // That data entry's location-specific data
                List<SpeciesDataLink> dataForSpeciesOnLocation = speciesDataEntry.getDataLinksByLocation(location);
                
                s += "<td>";
                
                if (dataType == null) 
                    s += "<div>"; // This <div> is a vital wrapper when dataType is null
                
                if (dataForSpeciesOnLocation.size() > 0) {
                    
                    Iterator<SpeciesDataLink> iDataLink = dataForSpeciesOnLocation.iterator();
                    while (iDataLink.hasNext()) {
                        SpeciesDataLink dataLink = iDataLink.next();
                        
                        // We don't want to include data links with negative order 
                        // factor (like the timing data links) in the combined table
                        if (dataType == null && excludedDataTypes.contains(dataLink.getType().getName()))
                            continue;
                        
                        if (dataType == null || dataType.equals(dataLink.getType().getName())) {
                            
                            if (dataType != null)
                                s += "<div class=\"rel-data-type-".concat(dataLink.getType().getIdentifier()).concat("\"") + ">";
                            
                            sums[dataTypeTotals.indexOf(dataLink.getType().getName())]++; // Increment relevant counter
                            sums[dataTypeTotals.size()]++; // Increment combined total (always at the "rightmost" position in the sums array)
                            String linkText = "";
                            if (dataType != null) {
                                linkText = dataLink.getNumYears();
                                
                                String comment = dataLink.getComment();
                                if (comment != null && !comment.isEmpty()) {
                                    int commentIndex = comments.indexOf(comment);
                                    if (commentIndex < 0) {
                                        comments.add(comment);
                                        commentIndex = comments.indexOf(comment);
                                    }
                                    linkText += "<sup>" + (commentIndex+1) + "</sup>"; 
                                }
                            }
                            
                            s += "<a href=\"" + dataLink.getUrl() + "\""
                                    + " class=\"rel-data-type-" + dataLink.getType().getIdentifier() + (dataType == null ? " species-data-link" : "") + "\""
                                    + " title=\"" + speciesDataEntry.getName() +": " + dataLink.getType().getLabel(cms) + ", " + location + (dataLink.getNumYears().isEmpty() ? "" : " ("+dataLink.getNumYears()+ " " + cms.label("label.seapop-species-data.year") + ")") + "\""
                                    + " target=\"_blank\""
                                    + ">" + linkText + "</a>";
                            
                            if (dataType != null)
                                s += "</div>";
                        }
                    }
                    
                } 
                
                if (dataType == null)
                    s += "</div>";
                
                s += "</td>";
            }
            
            if (dataType == null) {
                // No specified data type - print all aggregated sums
                for (int iSums = 0; iSums < sums.length; iSums++) {
                    try { if (excludedDataTypes.contains(dataTypeTotals.get(iSums))) continue; } catch (Exception e) {}
                    String dataTypeIdentifier = "total"; // Default: total - will be used if the next line throws an exception (which it should do at the last iteration)
                    try { dataTypeIdentifier = SpeciesDataLinkType.getIdentifierForName(dataTypeTotals.get(iSums)); } catch (Exception e) {}
                    s += "<td class=\"rel-data-type-" + dataTypeIdentifier + "\"><span>" + sums[iSums] + "</span></td>";
                }
            } 
            
            else {
                // Specified data type - print the single total sum
                s += "<td class=\"rel-data-type-t\"><span>" + sums[dataTypeTotals.indexOf(dataType)] + "</span></td>";
            }
            
            // End the row
            s += "</tr>\n\n";
            
            
        }
        
        s += "</table>\n\n";
        if (dataType != null && !comments.isEmpty()) {
            s += "<div class=\"species-data-table-comments\">";
            s += "<ol>";
            Iterator<String> iComments = comments.iterator();
            while (iComments.hasNext()) {
                s += "<li>" + iComments.next() + "</li>";
            }
            s += "</ol>";
            s += "</div>";
        }
        
        return s;
    }
    
    /**
     * Adds a species data entry.
     * 
     * @param speciesDataEntry The data entry to add.
     * @return This instance, updated.
     */
    private SpeciesDataCollection add(SpeciesData speciesDataEntry) {
        if (speciesDataEntry != null) {
            data.add(speciesDataEntry);
            updateNames(speciesDataEntry);
            updateLocations(speciesDataEntry);
        }
        return this;
    }
    /**
     * Updates the list of species names in this collection, after a data entry 
     * was added (in the {@link #add(no.seapop.data.SpeciesData)} method).
     * 
     * @param speciesDataEntry The newly added species data entry.
     */
    private void updateNames(SpeciesData speciesDataEntry) {
        if (!names.contains(speciesDataEntry.getName())) {
            names.add(speciesDataEntry.getName());
        }
    }
    /**
     * Updates the list of locations in this collection, after a data entry 
     * was added (in the {@link #add(no.seapop.data.SpeciesData)} method).
     * 
     * @param speciesDataEntry The newly added species data entry.
     */
    private void updateLocations(SpeciesData speciesDataEntry) {
        Iterator<String> iEntryLocations = speciesDataEntry.getLocations().iterator();
        while (iEntryLocations.hasNext()) {
            String entryLocation = iEntryLocations.next();
            if (!locations.contains(entryLocation))
                locations.add(entryLocation);
        }
    }
    /**
     * Sorts all entries in this collection by its group number.
     * 
     * @return This collection, sorted by group number.
     * @see SpeciesData#GROUP_NUMBER
     */
    private void sortByGroup() {
        Collections.sort(data, SpeciesData.GROUP_NUMBER);
    }
    
    /**
     * Sorts the list of data entries using the given comparator.
     * 
     * @param comp The comparator to use when sorting.
     * @return This instance, updated with newly sorted data entries.
     */
    public SpeciesDataCollection sort(Comparator<SpeciesData> comp) {
        Collections.sort(data, comp);
        return this;
    }
    
    /**
     * Gets the "raw" list of data entries that is at the foundation of this 
     * class.
     * 
     * @return The data entries contained in this instance.
     */
    public List<SpeciesData> get() { return this.data; }
    
    /**
     * Gets the number of data entries contained in this instance.
     * 
     * @return The number of data entries contained in this instance.
     */
    public int size() { return data.size(); }
}
