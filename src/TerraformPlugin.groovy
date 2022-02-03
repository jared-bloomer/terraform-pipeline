/**
 * Development Notes For Support version-specific workflows
 *
 * 1. Create a new class that extends TerraformPluginVersion
 * 2. Implement `apply` methods for any TerraformCommand and TerraformStage necessary
 * 3. Modify `strategyFor`, to return your new TerraformPluginVersion, for which
 *    ever set of terraform-versions that require your new workflow changes.
 *
 * See TerraformPluginVersion11 or TerraformPluginVersion12 for an example
 * before strating on your own.
 */
class TerraformPlugin implements TerraformValidateCommandPlugin,
                                 TerraformFormatCommandPlugin,
                                 TerraformInitCommandPlugin,
                                 TerraformPlanCommandPlugin,
                                 TerraformApplyCommandPlugin,
                                 TerraformValidateStagePlugin,
                                 Resettable {

    static String version
    static final String DEFAULT_VERSION = '0.11.0'
    public static TERRAFORM_VERSION_FILE = '.terraform-version'

    public static init() {
        def plugin = new TerraformPlugin()

        TerraformValidateCommand.addPlugin(plugin)
        TerraformFormatCommand.addPlugin(plugin)
        TerraformInitCommand.addPlugin(plugin)
        TerraformPlanCommand.addPlugin(plugin)
        TerraformApplyCommand.addPlugin(plugin)
        TerraformValidateStage.addPlugin(plugin)
    }

    public String detectVersion() {
        version = version ?: Jenkinsfile.readFile(TERRAFORM_VERSION_FILE) ?: DEFAULT_VERSION

        return version
    }

    public static String checkVersion() {
        return Jenkinsfile.build(pipelineConfiguration())
    }

    private static Closure pipelineConfiguration() {
        def closure = {
            node {
                deleteDir()
                checkout(scm)
                def plugin = new TerraformPlugin()

                return plugin.detectVersion()
            }
        }

        closure.resolveStrategy = Closure.DELEGATE_ONLY
        return closure
    }

    public TerraformPluginVersion strategyFor(String version) {
        // if (new SemanticVersion(version) >= new SemanticVersion('0.12.0')) should be used
        // here.  Unit tests pass with the above, but running Jenkinsfile in a pipeline context
        // does not.  Debug statements show that the above will return 0 when it should return 'true'.
        if ((new SemanticVersion(version) <=> new SemanticVersion('0.12.0')) >= 0) {
            return new TerraformPluginVersion12()
        }
        
        return new TerraformPluginVersion11()
    }

    static void withVersion(String userVersion) {
        this.version = userVersion
    }

    static  void reset() {
        this.version = null

        TerraformValidateCommand.reset()
        TerraformFormatCommand.reset()
        TerraformInitCommand.reset()
        TerraformPlanCommand.reset()
        TerraformApplyCommand.reset()
        TerraformValidateStage.reset()
    }

    @Override
    void apply(TerraformValidateCommand command) {
        applyToCommand(command)
    }

    @Override
    void apply(TerraformFormatCommand command) {
        applyToCommand(command)
    }

    @Override
    void apply(TerraformInitCommand command) {
        applyToCommand(command)
    }

    @Override
    void apply(TerraformPlanCommand command) {
        applyToCommand(command)
    }

    @Override
    void apply(TerraformApplyCommand command) {
        applyToCommand(command)
    }

    void applyToCommand(command) {
        def version = detectVersion()

        def strategy = strategyFor(version)
        strategy.apply(command)
    }

    @Override
    void apply(TerraformValidateStage validateStage) {
        validateStage.decorate(TerraformValidateStage.ALL, modifyValidateStage(validateStage))
    }

    public Closure modifyValidateStage(validateStage) {
        return { closure ->
            def version = detectVersion()

            def strategy = strategyFor(version)
            strategy.apply(validateStage)

            closure()
        }
    }
}
