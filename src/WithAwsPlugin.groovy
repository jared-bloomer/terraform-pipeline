import static TerraformEnvironmentStage.ALL

class WithAwsPlugin implements TerraformEnvironmentStagePlugin, Resettable {
    private static role
    private static duration

    public static void init() {
        WithAwsPlugin plugin = new WithAwsPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        String environment = stage.getEnvironment()

        stage.decorate(ALL, addWithAwsRole(environment))
    }

    public Closure addWithAwsRole(String environment) {
        return { closure ->
            String iamRole = getRole(environment)
            Integer sessionDuration = this.duration

            if (iamRole != null) {
                withAWS(role: iamRole, duration: sessionDuration) {
                    sh "echo Running AWS commands under the role: ${iamRole}"
                    closure()
                }
            } else {
                sh "echo no role found. Skipping withAWS"
                closure()
            }
        }
    }

    public static withRole(String role = null, Integer duration = 3600) {
        this.role = role
        this.duration = duration

        return this
    }

    public String getRole(String environment) {
        def tempRole = this.role

        if (tempRole == null) {
            tempRole = Jenkinsfile.instance.getEnv()['AWS_ROLE_ARN']
        }

        if (tempRole == null) {
            tempRole = Jenkinsfile.instance.getEnv()["${environment.toUpperCase()}_AWS_ROLE_ARN"]
        }

        if (tempRole == null) {
            tempRole = Jenkinsfile.instance.getEnv()["${environment}_AWS_ROLE_ARN"]
        }

        return tempRole
    }

    public static void reset() {
        this.role = null
        this.duration = 3600
    }
}
