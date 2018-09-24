package main.rule.engine;

import fj.function.Strings;
import main.rule.*;
import main.util.BuildFileParser;
import main.util.BuildFileParserFactory;
import main.util.NamedMethodMap;
import main.util.Utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RuleEngine {
    private static List<RuleChecker> ruleCheckerList = new ArrayList<>();

    static {

        ruleCheckerList.add(new InsecureAssymCryptoFinder());
        ruleCheckerList.add(new BrokenCryptoFinder());
        ruleCheckerList.add(new UntrustedPrngFinder());
        ruleCheckerList.add(new SSLSocketFactoryFinder());
        ruleCheckerList.add(new CustomTrustManagerFinder());
        ruleCheckerList.add(new HostNameVerifierFinder());
        ruleCheckerList.add(new BrokenHashFinder());
        ruleCheckerList.add(new ConstantKeyFinder());
        ruleCheckerList.add(new PredictableIVFinder());
        ruleCheckerList.add(new PBESaltFinder());
        ruleCheckerList.add(new PBEInterationCountFinder());
        ruleCheckerList.add(new PredictableSeedFinder());
        ruleCheckerList.add(new PredictableKeyStorePasswordFinder());
        ruleCheckerList.add(new HttpUrlFinder());
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.exit(1);
        }

        if (args[0].equals("jar")) {
            String projectJarPath = args[1];
            String projectDependencyPath = args[2];

            for (RuleChecker ruleChecker : ruleCheckerList) {
                ruleChecker.checkRule(EngineType.JAR, Arrays.asList(projectJarPath), projectDependencyPath);
            }

        } else if (args[0].equals("apk")) {
            String projectJarPath = args[1];

            String basePackage = Utils.getBasePackageNameFromApk(projectJarPath);
            System.out.println("*** Base package: " + basePackage);

            for (RuleChecker ruleChecker : ruleCheckerList) {
                ruleChecker.checkRule(EngineType.APK, Arrays.asList(projectJarPath), null);
            }
        } else if (args[0].equals("source")) {

            String projectRoot = args[1];
            String projectDependencyPath = args[2];

            BuildFileParser buildFileParser = BuildFileParserFactory.getBuildfileParser(projectRoot);

            Map<String, List<String>> moduleVsDependency = buildFileParser.getDependencyList();

            List<String> analyzedModules = new ArrayList<>();

            for (String module : moduleVsDependency.keySet()) {

                if (!analyzedModules.contains(module)) {

                    List<String> dependencies = moduleVsDependency.get(module);

                    for (String dependency : dependencies) {
                        String dependencyModule = dependency.substring(dependency.lastIndexOf('/'), dependency.length());
                        analyzedModules.add(dependencyModule);
                    }

                    for (RuleChecker ruleChecker : ruleCheckerList) {
                        ruleChecker.checkRule(EngineType.SOURCE, dependencies, projectDependencyPath);
                    }

                    NamedMethodMap.clearCallerCalleeGraph();
                }

            }


        }
    }
}