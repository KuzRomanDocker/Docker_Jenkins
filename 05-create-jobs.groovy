@Grab(group='org.yaml', module='snakeyaml', version='1.19')
import jenkins.model.*
import hudson.model.*
import hudson.tasks.LogRotator
import hudson.plugins.git.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import com.coravy.hudson.plugins.github.*
import com.cloudbees.jenkins.GitHubPushTrigger
import org.yaml.snakeyaml.Yaml

Jenkins jenkins = Jenkins.getInstance()

def pluginParameter = "workflow-aggregator github-pullrequest"
def plugins         = pluginParameter.split()
def pm              = jenkins.getPluginManager()
def installed       = false

plugins.each {
    if (!pm.getPlugin(it)) {
        println("Plugin ${it} not installed, skip creating jobs!")
    } else {
        installed = true
    }
}

if(installed) {
    def listExistingJob     = jenkins.items.collect { it.name }
    def listExistingViews   = jenkins.views.collect { it.name }
    def jobParameters       = new Yaml().load(new FileReader('/temp/job_list.yaml'))

    for (view in jobParameters) {
        if (jobParameters.any { listExistingViews.contains(view.key) }) {
            println("--- View ${view.key} already exist, skip")
        } else {
            println("--- Create new view ${view.key}")
            jenkins.addView(new ListView(view.key))
        }
        for (item in view.value) {
            if (view.value.any { listExistingJob.contains(item.key) }) {
                println("--- Job ${item.key} already exist, skip")
                continue
            } else {
                println("--- Create new job ${item.key}")

                def jobName             = item.key
                def jobDisabled         = view.value.get(item.key).getAt("jobDisabled")
                def enableGithubPolling = view.value.get(item.key).getAt("enableGithubPolling")
                def daysBuildsKeep      = view.value.get(item.key).getAt("daysBuildsKeep")
                def numberBuildsKeep    = view.value.get(item.key).getAt("numberBuildsKeep")
                def githubRepoUrl       = view.value.get(item.key).getAt("githubRepoUrl")
                def githubRepo          = view.value.get(item.key).getAt("githubRepo")
                def githubBranch        = view.value.get(item.key).getAt("githubBranch")
                def credentialId        = view.value.get(item.key).getAt("credentialId")
                def jenkinsfilePath     = view.value.get(item.key).getAt("jenkinsfilePath")

                def githubBranchList = githubBranch.split(', ')
                def branchConfig = new ArrayList<BranchSpec>()
                for (branch in githubBranchList) {
                    if( branch != null )
                    {
                        branchConfig.add(new BranchSpec(branch))
                    }
                    else
                    {
                        branchConfig.add(new BranchSpec("*/master"))
                    }
                }
                def userConfig          = [new UserRemoteConfig(githubRepo, null, null, credentialId)]
                def scm                 = new GitSCM(userConfig, branchConfig, false, [], null, null, null)
                def flowDefinition      = new CpsScmFlowDefinition(scm, jenkinsfilePath)

                flowDefinition.setLightweight(true)

                def job = new WorkflowJob(jenkins, jobName)
                job.definition = flowDefinition
                job.setConcurrentBuild(false)
                job.setDisabled(jobDisabled)
                job.addProperty(new BuildDiscarderProperty(new LogRotator(daysBuildsKeep, numberBuildsKeep, null, null)))
                job.addProperty(new GithubProjectProperty(githubRepoUrl))

                if (true == enableGithubPolling) {
                    job.addTrigger(new GitHubPushTrigger())
                }

                jenkins.save()
                jenkins.reload()
                hudson.model.Hudson.instance.getView(view.key).doAddJobToView(jobName)
            }
        }
    }
}
