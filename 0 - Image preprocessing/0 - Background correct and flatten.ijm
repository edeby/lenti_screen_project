name = getTitle();

close("\\Others");
run("32-bit");
run("Duplicate...", "title=gb");
run("Gaussian Blur...", "sigma=50");
imageCalculator( "Divide", name, "gb" );
selectImage(name);
run("Square Root");

setMinAndMax(0.00, 1.20);
//waitForUser;
setOption("ScaleConversions", true);
run("8-bit");