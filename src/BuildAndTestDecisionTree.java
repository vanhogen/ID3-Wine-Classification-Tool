import java.util.*;
import java.io.*;



/**
 * 
 * This class is responsible for the id3 ML handling of a dataset with a very
 * specific format. This is a CS 540 - Intro to AI specific project and was
 * designed with these specifics in mind
 * 
 * 
 * Copyright 2008, 2011, 2013 May be freely used for non-profit educational
 * purposes. To run after compiling, type: java BuildAndTestDecisionTree
 * <trainsetFilename> <testsetFilename> Eg, java BuildAndTestDecisionTree
 * train-hepatitis.data test-hepatitis.data where <trainsetFilename> and
 * <testsetFilename> are the input files of examples.
 * 
 * 
 * @author Student = Nathan Van Hogen
 * @author Professor = Jude Shavlik
 * @author TA = Nick Bridle
 * @author email = nathan.vanhogen@wisc.edu
 * @author netid = vanhogen
 * @author studentid = 906-321-5272
 *
 */
public class BuildAndTestDecisionTree {

    static int treeLevel = 0;

    /**
     * Handles opening files and function calls to parse, train, and categorize
     * datasets
     * 
     * @param args
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("You must call BuildAndTestDecisionTree as "
                    + "follows:\n\njava BuildAndTestDecisionTree "
                    + "<tunesetFilename> <testsetFilename>\n");
            System.exit(1);
        }
        
        List<Node> trainForest = new ArrayList<Node>();

        // Iterate and build the 101 trainsets ID3 trees.
        for(int i = 1; i <= 101; i++){

            String pathToBag = "C:\\Users\\Nathan\\workspace\\HW2\\wine-bagged\\";
            String fileName = "wine-b-" + i + ".data";
            pathToBag = pathToBag.concat(fileName);
            System.out.println(pathToBag);
            ListOfExamples trainExamples = new ListOfExamples();
            if(!trainExamples.ReadInExamplesFromFile(pathToBag)){
                System.out.println("Something went wrong reading the train data...giving up");
                System.exit(1);
            }else{
                Node root = new Node("root",null,null,null);
                buildDecisionTree(trainExamples,root);
                trainForest.add(root);
            }
        }
        
        //Last two files are the tune and train sets
        String tuneset = args[0];
        String testset = args[1];
        
        ListOfExamples tuneExamples = new ListOfExamples();
        ListOfExamples testExamples = new ListOfExamples();
        
        if(!tuneExamples.ReadInExamplesFromFile(tuneset) || !testExamples.ReadInExamplesFromFile(testset)){
            System.out.println("Something went wrong reading in the test and tune datasets...giving up");
            System.exit(1);
        }else{
            System.out.println("Data for tune set");
            System.out.println("=================");
            List<List<String>> predictionTuneSet = categorizeData(tuneExamples, trainForest);
            System.out.println("Data for test set");
            System.out.println("=================");
            List<List<String>> predictionTestSet = categorizeData(testExamples, trainForest);
            
            
        }

        Utilities.waitHere("Hit <enter> when ready to exit.");
    }

    /**
     * Uses forest of ID3 trees to predict the classification of an example
     * and then produces the accuracy
     * @param predictionSet - 2D array where rows = trees and columns = example prediction from
     * @param examples - the examples we are trying to predict
     * @param combinationThreshold
     */
    private static void plotAccuracy(List<List<String>> predictionSet,
            ListOfExamples examples, int combinationThreshold) {
        

        int numCorrect = 0;
        String tarLabel = examples.getOutputLabel().getFirstValue();
        String otherLabel = examples.getOutputLabel().getSecondValue();
        
        //Iterate through examples
        for(int i = 0; i < examples.size(); i++){
            //prediction count reset to 0 for every example
            int predictionCount = 0;
            
            //Iterate through trees guesses at examples
            for(int j = 0; j < predictionSet.size(); j++){
                
                //if odd tree
                if(j % 2 != 0){
                    
                    //Tallying tree guesses for same example
                    if(predictionSet.get(j).get(i).equals(tarLabel)){
                        
                        predictionCount++;

                    }
                    
                }
                
            }
            
            //Check if enough trees guessed 
            if(predictionCount >= combinationThreshold){

                if(tarLabel.equals(examples.get(i).getLabel())){

                    numCorrect++;
                }else{

                }
            }else{

                if(otherLabel.equals(examples.get(i).getLabel())){

                    numCorrect++;
                }else{

                }
                
                
            }   
        }
        
        System.out.println("For L = " + combinationThreshold);
        System.out.println("===========");
        System.out.println("Accuracy = " + numCorrect + "/" + examples.size() + " = " + ((float)numCorrect/(float)examples.size())*100 + "%\n");
        
    }

    /**
     * Builds a prediction array for use in plotting
     * @param examples - the example set we wish to predict on
     * @param trainForest - our forest of ID3 trees
     * @return - a 2D array where the ith row is the tree used to predict and
     *  the jth element is the example thats being predicted
     */
    private static List<List<String>> categorizeData(
            ListOfExamples examples, List<Node> trainForest) {

        List<List<String>> predictionData = new ArrayList<List<String>>();
        for(int i = 0; i < trainForest.size(); i++){
            List<String> treePrediction = new ArrayList<String>();
            predictionData.add(i, treePrediction);
            for(int j = 0; j < examples.size(); j++){
                treePrediction.add(j, 
                        guessClass(examples.get(j),trainForest.get(i)));
            }
        }
        System.out.println("predictiondata before plotting = " + predictionData.get(0).get(0));
        for(int i = 1; i <= 101; i++){
            plotAccuracy(predictionData, examples, i);
        }
        return predictionData;
    }

    /**
     * Takes in a parsed testing set to categorize individual examples and
     * reports errors to std.out
     * 
     * @param dataset
     *            - dataset to categorize
     * @param tree
     *            - Root node of decision tree
     * @return - returns true after completed categorization
     */
    private static Boolean categorizeData(ListOfExamples dataset, Node tree) {

        int countError = 0;

        for (int i = 0; i < dataset.size(); i++) {

            String guess = guessClass(dataset.get(i), tree);

            if (!(guess.equals(dataset.get(i).getLabel()))) {
                countError++;
                System.out.println("Error Categorizing\n==================");
                System.out.println(dataset.get(i).getName());
                System.out.println();
            }
        }

        System.out.println(
                "Total mistakes = " + countError + "/" + dataset.size());

        return true;
    }

    /**
     * Handles initial decision tree build and calls functions, including the
     * ID3 algorithm responsible for feature pick
     * 
     * @param dataset
     *            - training dataset
     * @param parent
     *            - root node to decision tree
     * @return - returns the parent node
     */
    private static Node buildDecisionTree(ListOfExamples dataset, Node parent) {

        // if no examples left to classify
        if (dataset.size() <= 0) {
            parent.setLeafValue(parent.getParent().getLeafValue());
            parent.setFeature("leaf");
            return parent;
        }

        // Check if all the features have been used
        if (dataset.getBinaryFeatures().length <= 0) {
            parent.setFeature("leaf");
            parent.setLeafValue(majorityClass(dataset));
            return parent;
        }

        // Check if all class types are the same, if so return Node with that
        // label
        Boolean doLabelsMatch = true;
        for (int i = 0; i < dataset.size(); i++) {

            if (!(dataset.get(0).getLabel()
                    .equals(dataset.get(i).getLabel()))) {

                doLabelsMatch = false;
            }

        }

        if (doLabelsMatch) {
            parent.setLeafValue(majorityClass(dataset));
            parent.setFeature("leaf");
            return parent;
        }

        // Attach feature to node and attach children nodes
        parent.setFeature(pickFeature(dataset));
        parent.setLeftChild(new Node(null, null, null, parent));
        parent.setRightChild(new Node(null, null, null, parent));
        parent.setTreeLevel(treeLevel);
        parent.setLeafValue(majorityClass(dataset));

        // Distribute List
        ListOfExamples leftList = dataset.getCopy();
        ListOfExamples rightList = dataset.getCopy();

        // left list keeps only the first value for the feature
        for (int i = 0; i < leftList.getBinaryFeatures().length; i++) {

            if (leftList.getFeatureName(i).equals(parent.getFeature())) {
                for (int j = 0; j < leftList.size(); j++) {

                    // find the pivot feature and remove any examples that do
                    // not match the second binary value for the feature
                    if (!leftList.get(j).get(i).equals(
                            leftList.getBinaryFeatures()[0].getFirstValue())) {

                        leftList.remove(j);

                    }

                }

            }

        }

        // right list keeps only second value for feature
        for (int i = 0; i < rightList.getBinaryFeatures().length; i++) {

            // find the pivot feature and remove any examples that do not match
            // the second binary value for the feature
            if (rightList.getFeatureName(i).equals(parent.getFeature())) {
                for (int j = 0; j < rightList.size(); j++) {

                    if (!rightList.get(j).get(i)
                            .equals(rightList.getBinaryFeatures()[0]
                                    .getSecondValue())) {

                        rightList.remove(j);

                    }

                }

            }

        }

        // Remove feature from feature list
        dataset.removeFeature(parent.getFeature());
        leftList.removeFeature(parent.getFeature());
        rightList.removeFeature(parent.getFeature());

        // Moving down the tree
        treeLevel++;

        if (leftList.getBinaryFeatures().length > 0 && leftList.size() > 0) {
            buildDecisionTree(leftList, parent.getLeftChild());

        } else {
            parent.setLeftChild(null);
        }

        if (rightList.getBinaryFeatures().length > 0 && rightList.size() > 0) {
            buildDecisionTree(rightList, parent.getRightChild());
        } else {
            parent.setRightChild(null);
        }

        // Moving up the tree
        treeLevel--;
        return parent;
    }

    /**
     * Prints tree in ascii format and trims null nodes. Does not print leaf
     * nodes as these are numerous with the current algorithm and do not offer
     * information but rather a way to terminate a branch. Only prints to the
     * fourth tree level
     * 
     * @param root - Root node of tree to print
     */
    private static void printTree(Node root) {

        // Trim if node is null and parent is not null, which would indicate the
        // root node
        if (root != null) {
            if (root.getFeature() == null) {

                // If this node is the left node
                if (root.getParent().getLeftChild().equals(root)) {
                    root.getParent().setLeftChild(null);
                } else {
                    root.getParent().setRightChild(null);
                }

            } else {

                // Only printing four levels deep
                if (root.getTreeLevel() < 4) {
                    // formatting for ascii tree
                    for (int i = 0; i < root.getTreeLevel(); i++) {
                        System.out.print("|   ");
                    }
                    // Skip printing leaf nodes
                    if (!root.getFeature().equals("leaf")) {
                        System.out.println(root.getFeature());
                    }
                }
                printTree(root.getLeftChild());
                printTree(root.getRightChild());

            }
        }
    }

    /**
     * This recursive method is responsible for guessing the class/category
     * of a given example with the knowledge given from the testing dataset
     * 
     * @param exOriginal - Example to classify
     * @param root - Originally root node to tree, then current node
     * @return - returns classification
     */
    private static String guessClass(Example exOriginal, Node root) {

        if (root == null) {
            root = root.getParent();
        }

        Example ex = exOriginal.getCopy(exOriginal.getParent());

        // TODO Optimization given a longer feature set, need to error check the
        // leaf variable though
        // There is an error - program currently runs by detecting null values
        // instead of properly setting leaf boolean
        // if(root.isLeaf()){
        // return root.getLeafValue();
        // }

        for (int i = 0; i < ex.size(); i++) {

            // if current nodes feature is the pivot variable for this dataset
            if (ex.getParent().getFeatureName(i).equals(root.getFeature())) {

                // if example feature value matches the dataset's first option
                // for the feature
                if (ex.get(i).equals(ex.getParent().getBinaryFeatures()[i]
                        .getFirstValue())) {

                    // if a child exists for first feature value then take it
                    if (root.getLeftChild() != null) {
                        return guessClass(ex, root.getLeftChild());
                    }

                    // the feature value matches the dataset's second option for
                    // the feature
                } else {

                    if (root.getRightChild() != null) {
                        return guessClass(ex, root.getRightChild());
                    }

                }

            }

        }

        return root.getLeafValue();
    }

    /**
     * Returns the string of the category with the highest information gain
     * 
     * @param dataset - dataset to calculate information gain on
     * @return - String of best feature according to ID3
     */
    private static String pickFeature(ListOfExamples dataset) {

        String bestFeature = "";
        double infoGained = 0;
        double compareInfoGain = 0;
        // Sums first binary values for features and class
        int featureAClassA = 0;
        int featureAClassB = 0;
        // Sums second binary values for features and class
        int featureBClassA = 0;
        int featureBClassB = 0;
        // Stored in stack for ease and speed
        BinaryFeature[] features = dataset.getBinaryFeatures();

        // iterate through features
        for (int j = 0; j < dataset.get(0).size(); j++) {

            // Clear sum variables
            featureAClassA = 0;
            featureAClassB = 0;
            featureBClassA = 0;
            featureBClassB = 0;

            // Iterate through and sum binary values of features
            for (int i = 0; i < dataset.size(); i++) {

                // if entry matches our first binary feature value then
                // increment featureA
                if (dataset.get(i).get(j).equals(features[j].getFirstValue())) {

                    // Increment around our class/category variable
                    if (dataset.get(i).getLabel()
                            .equals(dataset.getOutputLabel().getFirstValue())) {
                        featureAClassA++;
                    } else {
                        featureAClassB++;
                    }

                }
                // otherwise it will match our second feature value then
                // increment featureB
                else {

                    // Increment around our class/category variable
                    if (dataset.get(i).getLabel()
                            .equals(dataset.getOutputLabel().getFirstValue())) {
                        featureBClassA++;
                    } else {
                        featureBClassB++;
                    }

                }

            }

            // The feature has been summed so calculate Information Gain and
            // save if this feature has the highest information gain
            compareInfoGain = InformationGain(featureAClassA, featureAClassB,
                    featureBClassA, featureBClassB);
            if (infoGained <= compareInfoGain) {
                infoGained = compareInfoGain;
                bestFeature = dataset.getFeatureName(j);
            }

        }

        return bestFeature;

    }

    /**
     * Simple majority class/category calculation
     * @param dataset - dataset to calculate on
     * @return - String of class/categorization
     */
    private static String majorityClass(ListOfExamples dataset) {

        int sumFirst = 0;
        int sumSecond = 0;

        // count and some class variables
        for (int i = 0; i < dataset.size(); i++) {
            if (dataset.get(i).getLabel()
                    .equals(dataset.getOutputLabel().getFirstValue())) {

                sumFirst++;

            } else {
                sumSecond++;
            }
        }

        // return first class value if it was the majority
        if (sumFirst >= sumSecond) {
            return dataset.getOutputLabel().getFirstValue();
        }

        // return second class value
        return dataset.getOutputLabel().getSecondValue();

    }

    /**
     * Calculates Information Gain
     * 
     *                                        Class/Category to
     *                                            predict on
     *      
     *                                     ClassA           ClassB
     *                               |----------------|----------------|
     *Feature to check      FeatureA | featureAClassA | featureAClassB |
     *information gain on            |                |                |
     *                               |----------------|----------------|
     *                      FeatureB | featureBClassA | featureBClassB |
     *                               |                |                |
     *                               |----------------|----------------|
     */
    private static double InformationGain(double featureAClassA,
            double featureAClassB, double featureBClassA,
            double featureBClassB) {

        // Entropy for class/category
        double classEntropy = 0;
        double featureAEntropy = 0;
        double featureBEntropy = 0;
        // Entropy for feature when controlled on class/category
        double featureEntropy = 0;
        double infoGain = 0;

        double totalClassA = featureAClassA + featureBClassA;
        double totalClassB = featureAClassB + featureBClassB;
        double totalFeatureA = featureAClassA + featureAClassB;
        double totalFeatureB = featureBClassA + featureBClassB;
        double total = featureAClassA + featureAClassB + featureBClassA
                + featureBClassB;

        classEntropy = Entropy(totalClassA, totalClassB);
        if (Double.isNaN(classEntropy)) {
            classEntropy = 0;
        }

        // Algorithm to calculate feature Entropy with regard to class
        featureAEntropy = Entropy(featureAClassA, featureAClassB);
        if (Double.isNaN(featureAEntropy)) {
            featureAEntropy = 0;
        }

        featureBEntropy = Entropy(featureBClassA, featureBClassB);
        if (Double.isNaN(featureBEntropy)) {
            featureBEntropy = 0;
        }

        featureEntropy = ((totalFeatureA / total) * featureAEntropy)
                + ((totalFeatureB / total) * featureBEntropy);

        // Calculate Information Gain
        infoGain = classEntropy - featureEntropy;

        return infoGain;
    }

    /**
     * Entropy algorithm
     * @param a - value of feature for first class
     * @param b - value of feature for second class
     * @return - Entropy total
     */
    private static double Entropy(double a, double b) {

        double total = a + b;

        return (-((a / total) * (Math.log(a / total) / Math.log(2)))
                - ((b / total) * (Math.log(b / total) / Math.log(2))));

    }
}


/**
 * Node handles information needed by the decision tree
 * 
 * @author Nathan Van Hogen
 *
 */
class Node {

    private String feature;
    private Boolean isLeaf;
    private Node leftChild;
    private Node rightChild;
    private Node parent;
    private int treeLevel = 0;
    private String leafValue;

    //Constructor
    public Node(String feature, Node leftChild, Node rightChild, Node parent) {

        if (leftChild != null || rightChild != null) {
            isLeaf = false;
        } else {
            isLeaf = true;
        }
        this.parent = parent;
        this.feature = feature;
        this.leftChild = leftChild;
        this.rightChild = rightChild;

    }

    public Node getRightChild() {

        return rightChild;
    }

    public Node getLeftChild() {

        return leftChild;
    }

    public String getFeature() {

        return feature;
    }

    public void setFeature(String feature) {

        this.feature = feature;

    }

    public void setRightChild(Node rightChild) {

        if (rightChild == null && this.leftChild == null) {

            isLeaf = true;

        } else {

            isLeaf = false;

        }

        this.rightChild = rightChild;

    }

    public void setLeftChild(Node leftChild) {

        if (leftChild == null && this.rightChild == null) {

            isLeaf = true;

        } else {

            isLeaf = false;

        }

        this.leftChild = leftChild;

    }

    public Node getParent() {

        return parent;
    }

    public void setParent(Node parent) {

        this.parent = parent;
    }

    public int getTreeLevel() {

        return treeLevel;
    }

    public void setTreeLevel(int treeLevel) {

        this.treeLevel = treeLevel;
    }

    public Boolean isLeaf() {

        return this.isLeaf;
    }

    /*
     * Returns true if leafValue was set and false if node is not a leaf
     */
    public void setLeafValue(String leafValue) {

        this.leafValue = leafValue;

    }

    public String getLeafValue() {

        return leafValue;

    }
}



// This class, an extension of ArrayList, holds an individual example.
// The new method PrintFeatures() can be used to
// display the contents of the example.
// The items in the ArrayList are the feature values.
class Example extends ArrayList<String> {

    // The name of this example.
    private String name;

    // The output label of this example.
    private String label;

    // The data set in which this is one example.
    private ListOfExamples parent;

    // Constructor which stores the dataset which the example belongs to.
    public Example(ListOfExamples parent) {

        this.parent = parent;
    }

    // Print out this example in human-readable form.
    public void PrintFeatures() {

        System.out.print("Example " + name + ",  label = " + label + "\n");
        for (int i = 0; i < parent.getNumberOfFeatures(); i++) {
            System.out.print("     " + parent.getFeatureName(i) + " = "
                    + this.get(i) + "\n");
        }
    }

    public Example getCopy(ListOfExamples parent) {

        Example copy = new Example(parent);

        for (int i = 0; i < this.size(); i++) {
            copy.add(this.get(i));
        }
        copy.setName(this.name);
        copy.setLabel(this.label);
        return copy;

    }

    // Adds a feature value to the example.
    public void addFeatureValue(String value) {

        this.add(value);
    }

    // Accessor methods.
    public String getName() {

        return name;
    }

    public String getLabel() {

        return label;
    }

    // Mutator methods.
    public void setName(String name) {

        this.name = name;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public ListOfExamples getParent() {

        return parent;
    }
}



/*
 * This class holds all of our examples from one dataset (train OR test, not
 * BOTH). It extends the ArrayList class. Be sure you're not confused. We're
 * using TWO types of ArrayLists. An Example is an ArrayList of feature values,
 * while a ListOfExamples is an ArrayList of examples. Also, there is one
 * ListOfExamples for the TRAINING SET and one for the TESTING SET.
 */
class ListOfExamples extends ArrayList<Example> {

    // The name of the dataset.
    private String nameOfDataset = "";

    // The number of features per example in the dataset.
    private int numFeatures = -1;

    // An array of the parsed features in the data.
    private BinaryFeature[] features;

    // A binary feature representing the output label of the dataset.
    private BinaryFeature outputLabel;

    // The number of examples in the dataset.
    private int numExamples = -1;

    public ListOfExamples() {

    }

    public void removeFeature(String feature) {

        int removeIndex = -1;

        // find the target feature to be removed
        for (int i = 0; i < features.length; i++) {

            if (features[i].getName().equals(feature)) {
                removeIndex = i;
            }
        }

        // if last element in array the just use an empty array
        if (features.length == 1 || removeIndex == -1) {
            BinaryFeature[] shrinkFeatures =
                    new BinaryFeature[0];
            this.features = shrinkFeatures;
            // remove feature value in example
            for (int i = 0; i < this.size(); i++) {
                this.get(i).clear();
            }
        }else
        // otherwise deep copy the array to a smaller array
        if (removeIndex != -1) {

            BinaryFeature[] shrinkFeatures =
                    new BinaryFeature[features.length - 1];

            for (int i = 0; i < removeIndex; i++) {

                shrinkFeatures[i] = features[i];

            }

            for (int i = shrinkFeatures.length - 1; i + 1 > removeIndex; i--) {

                shrinkFeatures[i] = features[i + 1];
            }

            this.features = shrinkFeatures;

            // remove feature value in example
            for (int i = 0; i < this.size(); i++) {
                this.get(i).remove(removeIndex);
            }

        
        }

    }

    // Print out a high-level description of the dataset including its features.
    public void DescribeDataset() {

        System.out.println(
                "Dataset '" + nameOfDataset + "' contains " + numExamples
                        + " examples, each with " + numFeatures + " features.");
        System.out
                .println("Valid category labels: " + outputLabel.getFirstValue()
                        + ", " + outputLabel.getSecondValue());
        System.out
                .println("The feature names (with their possible values) are:");
        for (int i = 0; i < numFeatures; i++) {
            BinaryFeature f = features[i];
            System.out.println("   " + f.getName() + " (" + f.getFirstValue()
                    + " or " + f.getSecondValue() + ")");
        }
        System.out.println();
    }

    // Print out ALL the examples.
    public void PrintAllExamples() {

        System.out.println("List of Examples\n================");
        for (int i = 0; i < size(); i++) {
            Example thisExample = this.get(i);
            thisExample.PrintFeatures();
        }
    }

    // Print out the SPECIFIED example.
    public void PrintThisExample(int i) {

        Example thisExample = this.get(i);
        thisExample.PrintFeatures();
    }

    // Returns the number of features in the data.
    public int getNumberOfFeatures() {

        return numFeatures;
    }

    // Returns the name of the ith feature.
    public String getFeatureName(int i) {

        return features[i].getName();
    }

    // Takes the name of an input file and attempts to open it for parsing.
    // If it is successful, it reads the dataset into its internal structures.
    // Returns true if the read was successful.
    public boolean ReadInExamplesFromFile(String dataFile) {

        nameOfDataset = dataFile;

        // Try creating a scanner to read the input file.
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new File(dataFile));
        } catch (FileNotFoundException e) {
            return false;
        }

        // If the file was successfully opened, read the file
        this.parse(fileScanner);
        return true;
    }

    /**
     * Does the actual parsing work. We assume that the file is in proper
     * format.
     * 
     * @param fileScanner
     *            a Scanner which has been successfully opened to read the
     *            dataset file
     */
    public void parse(Scanner fileScanner) {

        // Read the number of features per example.
        numFeatures = Integer.parseInt(parseSingleToken(fileScanner));

        // Parse the features from the file.
        parseFeatures(fileScanner);

        // Read the two possible output label values.
        String labelName = "output";
        String firstValue = parseSingleToken(fileScanner);
        String secondValue = parseSingleToken(fileScanner);
        outputLabel = new BinaryFeature(labelName, firstValue, secondValue);

        // Read the number of examples from the file.
        numExamples = Integer.parseInt(parseSingleToken(fileScanner));

        parseExamples(fileScanner);
    }

    /**
     * Returns the first token encountered on a significant line in the file.
     * 
     * @param fileScanner
     *            a Scanner used to read the file.
     */
    private String parseSingleToken(Scanner fileScanner) {

        String line = findSignificantLine(fileScanner);

        // Once we find a significant line, parse the first token on the
        // line and return it.
        Scanner lineScanner = new Scanner(line);
        return lineScanner.next();
    }

    /**
     * Reads in the feature metadata from the file.
     * 
     * @param fileScanner
     *            a Scanner used to read the file.
     */
    private void parseFeatures(Scanner fileScanner) {

        // Initialize the array of features to fill.
        features = new BinaryFeature[numFeatures];

        for (int i = 0; i < numFeatures; i++) {
            String line = findSignificantLine(fileScanner);

            // Once we find a significant line, read the feature description
            // from it.
            Scanner lineScanner = new Scanner(line);
            String name = lineScanner.next();
            String dash = lineScanner.next(); // Skip the dash in the file.
            String firstValue = lineScanner.next();
            String secondValue = lineScanner.next();
            features[i] = new BinaryFeature(name, firstValue, secondValue);
        }
    }

    private void parseExamples(Scanner fileScanner) {

        // Parse the expected number of examples.
        for (int i = 0; i < numExamples; i++) {
            String line = findSignificantLine(fileScanner);
            Scanner lineScanner = new Scanner(line);

            // Parse a new example from the file.
            Example ex = new Example(this);

            String name = lineScanner.next();
            ex.setName(name);

            String label = lineScanner.next();
            ex.setLabel(label);

            // Iterate through the features and increment the count for any
            // feature
            // that has the first possible value.
            for (int j = 0; j < numFeatures; j++) {
                String feature = lineScanner.next();
                ex.addFeatureValue(feature);
            }

            // Add this example to the list.
            this.add(ex);
        }
    }

    /**
     * Returns the next line in the file F is significant (i.e. is not all
     * whitespace or a comment.
     * 
     * @param fileScanner
     *            a Scanner used to read the file
     */
    private String findSignificantLine(Scanner fileScanner) {

        // Keep scanning lines until we find a significant one.
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine().trim();
            if (isLineSignificant(line)) {
                return line;
            }
        }

        // If the file is in proper format, this should never happen.
        System.err.println("Unexpected problem in findSignificantLine.");

        return null;
    }

    /**
     * Returns whether the given line is significant (i.e., not blank or a
     * comment). The line should be trimmed before calling this.
     * 
     * @param line
     *            the line to check
     */
    private boolean isLineSignificant(String line) {

        // Blank lines are not significant.
        if (line.length() == 0) {
            return false;
        }

        // Lines which have consecutive forward slashes as their first two
        // characters are comments and are not significant.
        if (line.length() > 2 && line.substring(0, 2).equals("//")) {
            return false;
        }

        return true;
    }

    /*
     * Makes a deep copy of the contents of the list and the list properties
     * 
     * @return copy - the fully copied list
     * 
     * @return null - returns null if the list is currently empty
     */
    public ListOfExamples getCopy() {

        ListOfExamples copy = new ListOfExamples();
        int countExamples = 0;

        for (int i = 0; i < this.size(); i++) {
            copy.add(this.get(i).getCopy(copy));
            countExamples++;
        }

        BinaryFeature[] featuresCopy = new BinaryFeature[this.features.length];
        BinaryFeature outputLabelCopy = new BinaryFeature(
                this.outputLabel.getName(), this.outputLabel.getFirstValue(),
                this.outputLabel.getSecondValue());

        for (int i = 0; i < features.length; i++) {

            featuresCopy[i] = features[i];
        }

        copy.setBinaryFeatures(featuresCopy);
        copy.setNameOfDataset(nameOfDataset);
        copy.setNumExamples(countExamples);
        copy.setOutputLabel(outputLabelCopy);

        return copy;
    }

    public BinaryFeature getOutputLabel() {

        return outputLabel;
    }

    public String getNameOfDataset() {

        return nameOfDataset;

    }

    public BinaryFeature[] getBinaryFeatures() {

        return features;

    }

    public int getNumExamples() {

        return numExamples;

    }

    public void setBinaryFeatures(BinaryFeature[] features) {

        this.features = features;

    }

    public void setNumExamples(int numExamples) {

        this.numExamples = numExamples;
    }

    public void setNameOfDataset(String nameOfDataset) {

        this.nameOfDataset = nameOfDataset;
    }

    public void setOutputLabel(BinaryFeature outputLabel) {

        this.outputLabel = outputLabel;

    }

}



/**
 * Represents a single binary feature with two String values.
 */
class BinaryFeature {

    private String name;
    private String firstValue;
    private String secondValue;

    public BinaryFeature(String name, String first, String second) {

        this.name = name;
        firstValue = first;
        secondValue = second;
    }

    public String getName() {

        return name;
    }

    public String getFirstValue() {

        return firstValue;
    }

    public String getSecondValue() {

        return secondValue;
    }
}



class Utilities {

    // This method can be used to wait until you're ready to proceed.
    public static void waitHere(String msg) {

        System.out.print("\n" + msg);
        try {
            System.in.read();
        } catch (Exception e) {
        } // Ignore any errors while reading.
    }
}
