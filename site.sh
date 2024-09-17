#!/bin/bash
##
## Script te generate and deploy the site to Github Pages.
##

export NVP_API_KEY=$(op read "op://sxetjycambbujpjbtcztimp7xa/NVP API Key/credential")

export TEST_SKIP="false"
export DEPENDENCY_SKIP="false"
export PUBLISH_DRYRUN="false"

mvn -Ddependency.skip="${DEPENDENCY_SKIP}" \
    -DconnectionString="jdbc:postgresql://$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/server"):$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/port")/$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/database")?sslmode=require" \
    -DdatabaseUser="$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/username")" \
    -DdatabasePassword="$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/password")" \
    -DdatabaseDriverName="org.postgresql.Driver" \
    -Dscmpublish.dryRun="${PUBLISH_DRYRUN}" \
    -DskipTests="${TEST_SKIP}" \
    clean verify site site:stage scm-publish:publish-scm
