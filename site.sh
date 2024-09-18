#!/bin/bash
##
## Script te generate and deploy the site to Github Pages.
##

export NVP_API_KEY=$(op read "op://sxetjycambbujpjbtcztimp7xa/NVP API Key/credential")

export SKIP_TESTS="true"
export SKIP_DEPENDENCY_CHECK="true"
export DRY_RUN_PUBLISH="true"

mvn -Ddependency.skip="${SKIP_DEPENDENCY_CHECK}" \
    -DconnectionString="jdbc:postgresql://$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/server"):$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/port")/$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/database")?sslmode=require" \
    -DdatabaseUser="$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/username")" \
    -DdatabasePassword="$(op read "op://sxetjycambbujpjbtcztimp7xa/CVE database/password")" \
    -DdatabaseDriverName="org.postgresql.Driver" \
    -Dscmpublish.dryRun="${DRY_RUN_PUBLISH}" \
    -DskipTests="${SKIP_TESTS}" \
    clean verify site site:stage scm-publish:publish-scm
