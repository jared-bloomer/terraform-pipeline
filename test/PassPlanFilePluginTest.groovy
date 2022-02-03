import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ResetStaticStateExtension.class)
class PassPlanFilePluginTest {
    @Nested
    public class Init {
        @Test
        void modifiesTerraformEnvironmentStageCommand() {
            PassPlanFilePlugin.init()

            Collection actualPlugins = TerraformEnvironmentStage.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PassPlanFilePlugin.class)))
        }

        @Test
        void modifiesTerraformPlanCommand() {
            PassPlanFilePlugin.init()

            Collection actualPlugins = TerraformPlanCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PassPlanFilePlugin.class)))
        }

        @Test
        void modifiesTerraformApplyCommand() {
            PassPlanFilePlugin.init()

            Collection actualPlugins = TerraformApplyCommand.getPlugins()
            assertThat(actualPlugins, hasItem(instanceOf(PassPlanFilePlugin.class)))
        }

    }

    @Nested
    public class Apply {

        @Test
        void decoratesTheTerraformEnvironmentStage()  {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            def environment = spy(new TerraformEnvironmentStage())
            plugin.apply(environment)

            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.PLAN_COMMAND), any(Closure.class))
            verify(environment, times(1)).decorate(eq(TerraformEnvironmentStage.APPLY_COMMAND), any(Closure.class))
        }

        @Test
        void addsArgumentToTerraformPlan() {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            TerraformPlanCommand command = new TerraformPlanCommand("dev")
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("-out=tfplan-dev"))
        }

        @Test
        void addsArgumentToTerraformApply() {
            PassPlanFilePlugin plugin = new PassPlanFilePlugin()
            TerraformApplyCommand command = new TerraformApplyCommand("dev")
            plugin.apply(command)

            String result = command.toString()
            assertThat(result, containsString("tfplan-dev"))
        }

    }

    @Nested
    public class StashPlan {

        @Test
        void runsStashPlan() {
            def plugin = new PassPlanFilePlugin()
            def workflowScript = new MockWorkflowScript()

            def stashClosure = plugin.stashPlan('dev')
            stashClosure.delegate = workflowScript
            stashClosure.call { } // we don't care about the inner closure, so we're passing an empty one

            verify(workflowScript, times(1)).stash()
        }

        @Test
        void usesCurrentDirectoryByDefault() {
            def plugin = new PassPlanFilePlugin()
            def workflowScript = new MockWorkflowScript()

            def stashClosure = plugin.stashPlan('dev')
            stashClosure.delegate = workflowScript
            stashClosure.call { } // we don't care about the inner closure, so we're passing an empty one

            verify(workflowScript, times(1)).dir('./', any(Closure))
        }

        @Test
        void usesDirectoryIfGiven() {
            def plugin = new PassPlanFilePlugin()
            def workflowScript = new MockWorkflowScript()
            def expectedDirectory = 'myDir'
            plugin.withDirectory(expectedDirectory)

            def stashClosure = plugin.stashPlan('dev')
            stashClosure.delegate = workflowScript
            stashClosure.call { } // we don't care about the inner closure, so we're passing an empty one

            verify(workflowScript, times(1)).dir(expectedDirectory, any(Closure))
        }

    }

    @Nested
    public class UnstashPlan {

        @Test
        void runsUnstashPlan() {
            def plugin = new PassPlanFilePlugin()
            def workflowScript = new MockWorkflowScript()

            def unstashClosure = plugin.unstashPlan('dev')
            unstashClosure.delegate = workflowScript
            stashClosure.call { } // we don't care about the inner closure, so we're passing an empty one

            verify(workflowScript, times(1)).unstash()
        }

        @Test
        void usesCurrentDirectoryByDefault() {
            def plugin = new PassPlanFilePlugin()
            def workflowScript = new MockWorkflowScript()

            def unstashClosure = plugin.unstashPlan('dev')
            unstashClosure.delegate = workflowScript
            unstashClosure.call { } // we don't care about the inner closure, so we're passing an empty one

            verify(workflowScript, times(1)).dir('./', any(Closure))
        }

        @Test
        void usesDirectoryIfGiven() {
            def plugin = new PassPlanFilePlugin()
            def workflowScript = new MockWorkflowScript()
            def expectedDirectory = 'myDir'
            plugin.withDirectory(expectedDirectory)

            def unstashClosure = plugin.unstashPlan('dev')
            unstashClosure.delegate = workflowScript
            unstashClosure.call { } // we don't care about the inner closure, so we're passing an empty one

            verify(workflowScript, times(1)).dir(expectedDirectory, any(Closure))
        }

    }

}
