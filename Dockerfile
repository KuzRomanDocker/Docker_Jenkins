FROM jenkins/jenkins:jdk11

# Skip initial setup
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Dfile.encoding=UTF8 -Dsun.jnu.encoding=UTF8"

USER jenkins

# Install plugins
#COPY plugins.txt /usr/share/jenkins/plugins.txt
#RUN jenkins-plugin-cli --verbose -f /usr/share/jenkins/plugins.txt

#
#COPY PROD.xml /PROD.xml
COPY job_list.yaml /temp/job_list.yaml
COPY apache-maven-3.8.1-bin.tar.gz /var/jenkins_home/downloads/apache-maven-3.8.1-bin.tar.gz 
#
#COPY credentials.xml /usr/share/jenkins/ref/credentials.xml

# Copy in the scripts
#COPY PROD.groovy /usr/share/jenkins/ref/init.groovy.d/PROD.groovy
COPY  *.groovy /usr/share/jenkins/ref/init.groovy.d/

ENV LANG=en_US.UTF-8
