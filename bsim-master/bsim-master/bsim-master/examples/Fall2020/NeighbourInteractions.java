package Fall2020;

import bsim.BSim;
import bsim.BSimChemicalField;
import bsim.BSimTicker;
import bsim.BSimUtils;
import bsim.capsule.BSimCapsuleBacterium;
import bsim.capsule.Mover;
import bsim.capsule.RelaxationMoverGrid;
import bsim.draw.BSimDrawer;
import bsim.draw.BSimP3DDrawer;
import bsim.export.BSimLogger;
import bsim.export.BSimPngExporter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import processing.core.PConstants;
import processing.core.PGraphics3D;

import javax.vecmath.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.lang.Math;
import java.util.Arrays;
import java.io.File;

// Fall 2020 Project
public class NeighbourInteractions {

    @Parameter(names = "-export", description = "Enable export mode.")
    private boolean export = true;

    @Parameter(names = "-dim", arity = 3, description = "The dimensions (x, y, z) of simulation environment (um).")
    public List<Double> simDimensions = new ArrayList<>(Arrays.asList(new Double[] {198.0,159.0, 1.}));

    // Boundaries
    @Parameter(names = "-fixedbounds", description = "Enable fixed boundaries. (If not, one boundary will be leaky as real uf chamber).")
    private boolean fixedBounds = true;

    // Grid ->
    // 52x42 -> 546
    // 100x86 -> 2150
    // Random:
    // 50x42 -> 250
    // 100x85 -> 1000
    // Density (cell number)
    @Parameter(names = "-pop", arity = 1, description = "Initial seed population (n_total).")
    public int initialPopulation = 10;

    // A:R ratio
    @Parameter(names = "-ratio", arity = 1, description = "Ratio of initial populations (proportion of activators).")
    public double populationRatio = 0.0;

    @Parameter(names="-gr_stdv",arity=1,description = "growth rate standard deviation")
    public double growth_stdv=0.05;

    @Parameter(names="-gr_mean",arity=1,description = "growth rate mean")
    public double growth_mean=0.2;

    @Parameter(names="-len_stdv",arity=1,description = "elongation threshold standard deviation")
    public double length_stdv=0.1;

    @Parameter(names="-len_mean",arity=1,description = "elongation threshold mean")
    public double length_mean=7.0;

    /**
     * Whether to enable growth in the ticker etc. or not...
     */
    private static final boolean WITH_GROWTH = true;

    // this is the very first function that runs in the simulation
    public static void main(String[] args) {

        // creates new simulation data object
        NeighbourInteractions bsim_ex = new NeighbourInteractions();

        // starts up JCommander which allows you to read options from the command line more easily
        new JCommander(bsim_ex, args);

        // begins simulation
        bsim_ex.run();
    }

    // This function does a lot. let's break it down
    // 1. Initializes simulation (how long it runs, etc)
    // 2. Reads initial position data for bacteria and creates new bacterium objects accordingly
    // 3. Runs simulation loop until the simulation time is up
    // |----> Updates bacteria positions according to the forces acting on them
    // |----> Logs all data from the simulation into an excel sheet
    // |----> Saves images of simulation
    public void run() {
        // initializes simulation size
        final double simX = simDimensions.get(0);
        final double simY = simDimensions.get(1);
        final double simZ = simDimensions.get(2);

        // saves the exact time when the simulation started
        long simulationStartTime = System.nanoTime();

        // create the simulation object
        final BSim sim = new BSim();
        sim.setDt(0.05);				    // Simulation Timestep
        sim.setSimulationTime(100);       // 36000 = 10 hours; 600 minutes. //3600 = 1hr, 1800 = 30 mins
//        sim.setSimulationTime(60000);
        sim.setTimeFormat("0.00");		    // Time Format for display
        sim.setBound(simX, simY, simZ);		// Simulation Boundaries


        // Boundaries are not periodic
        sim.setSolid(true, true, true);

        // Leaky -> bacteria can escape from sides
        if(!fixedBounds) {
            sim.setLeaky(true, true, true, true,false, false);
            sim.setLeakyRate(0.1/60.0, 0.1/60.0, 0.1/60.0, 0.1/60.0, 0, 0);
        }

        /*********************************************************
         * Create the bacteria
         */
        // Separate lists of bacteria in case we want to manipulate the species individually
        final ArrayList<Bacterium> bac = new ArrayList();
        // Track all of the bacteria in the simulation, for use of common methods etc
        final ArrayList<BSimCapsuleBacterium> bacteriaAll = new ArrayList();

        Random bacRng = new Random(); //random number generator
        bacRng.setSeed(50); // initializes random number generator

        // empty list which will later contain the endpoint positions
        double[][] initEndpoints = new double[4][];

        // gets the location of the file that is currently running
        String systemPath = new File("").getAbsolutePath()+"\\SingleCellSims";

        // creates a new csvreader object which can extract data from .csv files
        BufferedReader csvReader = null;
        try {
            // try reading the initial position file
            csvReader = new BufferedReader(new FileReader("C:\\Users\\catie\\Documents\\code\\Bingalls\\bsim-master\\bsim-master\\bsim-master\\examples\\Fall2020\\onecell.csv"));
        } catch (FileNotFoundException e) {
            // if that doesn't work, print out an error
            e.printStackTrace();
        }

        // if loading the file works, try reading the content
        try {
            // goes through each row of the excel sheet and pulls out the initial positions
            String row = csvReader.readLine();
            int i=0;
            while (row != null) {
                // row.split takes a single line of the excel sheet and chops it up into the columns
                // maptodouble then takes the values in those columns and converts them to Java double data format
                // toarray then finally converts the data into an array
                initEndpoints[i] = Arrays.stream(row.split(",")).mapToDouble(Double::parseDouble).toArray();
                row = csvReader.readLine();
                i=i+1;
            }
            csvReader.close(); //finally, close the file once all data is extracted
        } catch(IOException e) {
            e.printStackTrace(); // if there is an error, this will just print out the message
        }

        // now that the data is extracted, we can create the bacterium objects
        for(int j=0;j<initEndpoints[0].length;j++){
            // initializes the endpoints of each bacterium from the array of endpoints
            Vector3d x1 = new Vector3d(initEndpoints[0][j]/13.89,initEndpoints[1][j]/13.89,bacRng.nextDouble()*0.1*(simZ - 0.1)/2.0);
            Vector3d x2 = new Vector3d(initEndpoints[2][j]/13.89,initEndpoints[3][j]/13.89,bacRng.nextDouble()*0.1*(simZ - 0.1)/2.0);
            // note that the endpoint positions are scaled by 13.89, since the images are a bit more than 2000 pixels wide
            // while the simulation is rougly 200 micrometers. the conversion factor ends up being 13.89

            // creates a new bacterium object whos' endpoints correspond to the above data
            Bacterium bac0 = new Bacterium(sim,x1,x2);

            // determine the distance between the endpoints
            Vector3d dispx1x2=new Vector3d();
            dispx1x2.sub(x2,x1);
            double length0=dispx1x2.length();

            // if the data suggests that a bacterium is larger than L_max, that bacterium will instead
            // be initialized with length 1. Otherwise, we set the length in the following if statement
            if(length0<bac0.L_max) {
                bac0.initialise(length0, x1, x2);
            }

            // assigns a growth rate and a division length to each bacterium according to a normal distribution
            double growthRate=growth_stdv*bacRng.nextGaussian() + growth_mean;
            bac0.setK_growth(growthRate);

            double lengthThreshold=length_stdv*bacRng.nextGaussian()+length_mean;
            bac0.setElongationThreshold(lengthThreshold);

            // adds the newly created bacterium to our lists for tracking purposes
            bac.add(bac0);
            bacteriaAll.add(bac0);
        }

        // Set up stuff for growth.
        final ArrayList<Bacterium> bac_born = new ArrayList();
        final ArrayList<Bacterium> bac_dead = new ArrayList();

        // internal machinery - dont worry about this line
        final Mover mover;
        mover = new RelaxationMoverGrid(bacteriaAll, sim);

        /*********************************************************
         * Set up the ticker
         */
        final int LOG_INTERVAL = 100; // logs data every 100 timesteps

        // This one is a bit long too. Let's break it up
        // 1. Begins an "action" -> this represents one timestep
        // 2. Tells each bacterium to perform their action() function
        // 3. Updates each chemical field in the simulation
        // 4. Bacteria are then told to grow()
        // 5. bacteria which are longer than their threshold are told to divide()
        // 6. forces are applied and bacteria move around
        // 7. bacteria which are out of bounds are removed from the simulation
        BSimTicker ticker = new BSimTicker() {
            @Override
            public void tick() {
                // ********************************************** Action
                long startTimeAction = System.nanoTime();

                for(BSimCapsuleBacterium b : bacteriaAll) {
                    b.action(); //bacteria do action at each time step
                }

                long endTimeAction = System.nanoTime();
                if((sim.getTimestep() % LOG_INTERVAL) == 0) {
                    System.out.println("Action update for " + bacteriaAll.size() + " bacteria took " + (endTimeAction - startTimeAction)/1e6 + " ms.");
                }

                // ********************************************** Chemical fields
                startTimeAction = System.nanoTime();

                endTimeAction = System.nanoTime();
                if((sim.getTimestep() % LOG_INTERVAL) == 0) {
                    System.out.println("Chemical field update took " + (endTimeAction - startTimeAction)/1e6 + " ms.");
                }

                // ********************************************** Growth related activities if enabled.
                if(WITH_GROWTH) {

                    // ********************************************** Growth and division
                    startTimeAction = System.nanoTime(); //start action timer

                    for (Bacterium b : bac) {
                        b.grow();

                        // Divide if grown past threshold
                        if (b.L >= b.L_th) {
                            bac_born.add(b.divide());
                        }
                    }
                    bac.addAll(bac_born);
                    bacteriaAll.addAll(bac_born);
                    bac_born.clear();

                    endTimeAction = System.nanoTime();
                    if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                        System.out.println("Growth and division took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
                    }
                    //above: prints out information abt bacteria when u want it to
                    // ********************************************** Neighbour interactions
                    startTimeAction = System.nanoTime();

                    mover.move();

                    endTimeAction = System.nanoTime();
                    if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                        System.out.println("Wall and neighbour interactions took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
                    }

                    // ********************************************** Boundaries/removal
                    startTimeAction = System.nanoTime();
                    // Removal
                    for (Bacterium b : bac) {
//                         Kick out if past the top or bottom boundaries
//                        if ((b.x1.y < 0) && (b.x2.y < 0)) {
//                            act_dead.add(b);
//                        }
//                        if ((b.x1.y > sim.getBound().y) && (b.x2.y > sim.getBound().y)) {
//                            act_dead.add(b);
//                        }
                        // kick out if past any boundary
                        if(b.position.x < 0 || b.position.x > sim.getBound().x || b.position.y < 0 || b.position.y > sim.getBound().y || b.position.z < 0 || b.position.z > sim.getBound().z){
                            bac_dead.add(b);
                        } //bacteria out of bounds = dead
                    }
                    bac.removeAll(bac_dead);
                    bacteriaAll.removeAll(bac_dead);
                    bac_dead.clear();

                    endTimeAction = System.nanoTime();
                    if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                        System.out.println("Death and removal took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
                    }
                }

                startTimeAction = System.nanoTime();

                endTimeAction = System.nanoTime();
                if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                    System.out.println("Switch took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
                }

            }
        };

        sim.setTicker(ticker);

        
//      the rest of the code is the drawer (makes simulation images) and data logger (makes csv files)

        /*********************************************************
         * Set up the drawer
         */
        BSimDrawer drawer = new BSimP3DDrawer(sim, 2752, 2208) {
            /**
             * Draw the default cuboid boundary of the simulation as a partially transparent box
             * with a wireframe outline surrounding it.
             */
            @Override
            public void boundaries() {
                p3d.noFill();
                p3d.stroke(128, 128, 255);
                p3d.pushMatrix();
                p3d.translate((float)boundCentre.x,(float)boundCentre.y,(float)boundCentre.z);
                p3d.box((float)bound.x, (float)bound.y, (float)bound.z);
                p3d.popMatrix();
                p3d.noStroke();
            }

            @Override
            public void draw(Graphics2D g) {
                p3d.beginDraw();

                if(!cameraIsInitialised){
                    // camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
                    p3d.camera((float)bound.x*0.5f, (float)bound.y*0.5f,
                            // Set the Z offset to the largest of X/Y dimensions for a reasonable zoom-out distance:
                            simX > simY ? (float)simX : (float)simY,
//                            10,
                            (float)bound.x*0.5f, (float)bound.y*0.5f, 0,
                            0,1,0);
                    cameraIsInitialised = true;
                }

                p3d.textFont(font);
                p3d.textMode(PConstants.SCREEN);

                p3d.sphereDetail(10);
                p3d.noStroke();
                p3d.background(255, 255,255);

                scene(p3d);
                boundaries();
                time();

                p3d.endDraw();
                g.drawImage(p3d.image, 0,0, null);
            }

            /**
             * Draw the formatted simulation time to screen.
             */
            @Override
            public void time() {
                p3d.fill(0);
//                p3d.text(sim.getFormattedTimeHours(), 50, 50);
                p3d.text(sim.getFormattedTime(), 50, 50);
            }

            @Override
            public void scene(PGraphics3D p3d) {
                p3d.ambientLight(128, 128, 128);
                p3d.directionalLight(128, 128, 128, 1, 1, -1);

//                draw(bac_act, new Color(55, 126, 184));
//                draw(bac_rep, new Color(228, 26, 28));

                for(Bacterium b : bac) {
                    draw(b, Color.blue);
                }

            }
        };
        sim.setDrawer(drawer);

        export=true;
        if(export) {
            String simParameters = "" + BSimUtils.timeStamp() + "__dim_" + simX + "_" + simY + "_" + simZ
                    + "__ip_" + initialPopulation
                    + "__pr_" + populationRatio;

            if(fixedBounds){
                simParameters += "__fixedBounds";
            } else {
                simParameters += "__leakyBounds";
            }

            String filePath = BSimUtils.generateDirectoryPath(systemPath +"/" + simParameters + "/");
//            String filePath = BSimUtils.generateDirectoryPath("/home/am6465/tmp-results/" + simParameters + "/");

            /*********************************************************
             * Various properties of the simulation, for future reference.
             */
            BSimLogger metaLogger = new BSimLogger(sim, filePath + "simInfo.txt") {
                @Override
                public void before() {
                    super.before();
                    write("Simulation metadata.");
                    write("Catie Terrey Fall 2020."); //change name when new person :)
                    write("Simulation dimensions: (" + simX + ", " + simY + ", " + simZ + ")");
                    write("Initial population: "+ initialPopulation);
                    write("Ratio " + populationRatio);



                    if(fixedBounds){
                        write("Boundaries: fixed");
                    } else {
                        write("Boundaries: leaky");
                    }
                }

                @Override
                public void during() {

                }
            };
            metaLogger.setDt(10);//3600);			// Set export time step
            sim.addExporter(metaLogger);

            BSimLogger posLogger = new BSimLogger(sim, filePath + "position.csv") {
                DecimalFormat formatter = new DecimalFormat("###.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));

                @Override
                public void before() {
                    super.before();
                    write("per Act; per Rep; id, p1x, p1y, p1z, p2x, p2y, p2z, growth_rate,directions");
                }

                @Override
                public void during() {
                    String buffer = new String();

                    buffer += sim.getFormattedTime() + "\n";
                    write(buffer);

                    write("acts");

                    buffer = "";
                    for(BSimCapsuleBacterium b : bacteriaAll) {
                        buffer += b.id + "," + formatter.format(b.x1.x)
                                + "," + formatter.format(b.x1.y)
                                + "," + formatter.format(b.x1.z)
                                + "," + formatter.format(b.x2.x)
                                + "," + formatter.format(b.x2.y)
                                + "," + formatter.format(b.x2.z)
                                + "," + formatter.format(b.getK_growth())
                                + "\n";
                    }

                    write(buffer);


                }
            };
            posLogger.setDt(10);			// set export time step for csv file
            sim.addExporter(posLogger);


            BSimLogger sumLogger = new BSimLogger(sim, filePath + "summary.csv") {


                @Override
                public void before() {
                    super.before();
                    write("time,id, status, p1x, p1y, p1z, p2x, p2y, p2z, px, py, pz, growth_rate, directions");
                }

                @Override
                public void during() {
                    String buffer = new String();
                    buffer = "";
                    for(BSimCapsuleBacterium b : bacteriaAll) {
                        buffer += sim.getFormattedTime()+","+b.id
                                + "," + b.getInfected()
                                + "," + b.x1.x
                                + "," + b.x1.y
                                + "," + b.x1.z
                                + "," + b.x2.x
                                + "," + b.x2.y
                                + "," + b.x2.z
                                + "," + b.position.x
                                + "," + b.position.y
                                + "," + b.position.z
                                + "," + b.getK_growth()
                                + "," + b.direction()
                                + "\n";
                    }

                    write(buffer);
                }
            };
            sumLogger.setDt(10);			// Set export time step
            sim.addExporter(sumLogger);




            /**
             * Export a rendered image file
             */
            BSimPngExporter imageExporter = new BSimPngExporter(sim, drawer, filePath );
            imageExporter.setDt(10); //this is how often it will output a frame
            sim.addExporter(imageExporter);

            sim.export();


            /**
             * Drawing a java plot once we're done?
             * See TwoCellsSplitGRNTest
             */

        } else {
            sim.preview();
        }

        long simulationEndTime = System.nanoTime();

        System.out.println("Total simulation time: " + (simulationEndTime - simulationStartTime)/1e9 + " sec.");
    }
}
