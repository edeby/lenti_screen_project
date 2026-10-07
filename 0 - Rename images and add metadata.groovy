def project = getProject()

def entries = project.getImageList()

entries.each{ entry ->
    def uris = entry.getURIs()
    def path = Paths.get( uris[0] )
    
    def fileName = path.getFileName()
    def parent = path.getParent().getFileName().toString()
    println parent
    // Extract metadata
    def elements = parent.split(" ", 10)
    // Add metadata
    entry.putMetadataValue( "Date", elements[0] )
    entry.putMetadataValue( "Organoid type", elements[1] )
    entry.putMetadataValue( "Parental Strain", elements[2] )
    entry.putMetadataValue( "Batch number", elements[3] )
    entry.putMetadataValue( "Timepoint", elements[4] )
    
    //Rename image
    // Get name
    entryName = entry.getImageName().split("[_.]")
    seriesNumber = entryName[entryName.size()-2]
    entry.putMetadataValue( "Series Number", "seriesNumber" )
    
    entry.putMetadataValue( "Condition", entryName[0] )
    def newName = "${elements[0]}  ${elements[1]} ${elements[2]} ${elements[3]} ${elements[4]}  ${entryName[0]} - $seriesNumber"
    entry.setImageName( newName )

}


import java.nio.file.Paths