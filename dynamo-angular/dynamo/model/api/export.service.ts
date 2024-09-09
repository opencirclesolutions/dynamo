/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional }                      from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams,
         HttpResponse, HttpEvent, HttpParameterCodec, HttpContext 
        }       from '@angular/common/http';
import { CustomHttpParameterCodec }                          from '../encoder';
import { Observable }                                        from 'rxjs';

// @ts-ignore
import { SearchModel } from '../model/searchModel';

// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';



@Injectable({
  providedIn: 'root'
})
export class ExportService {

    protected basePath = 'http://localhost:8080';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();
    public encoder: HttpParameterCodec;

    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string|string[], @Optional() configuration: Configuration) {
        if (configuration) {
            this.configuration = configuration;
        }
        if (typeof this.configuration.basePath !== 'string') {
            if (Array.isArray(basePath) && basePath.length > 0) {
                basePath = basePath[0];
            }

            if (typeof basePath !== 'string') {
                basePath = this.basePath;
            }
            this.configuration.basePath = basePath;
        }
        this.encoder = this.configuration.encoder || new CustomHttpParameterCodec();
    }


    // @ts-ignore
    private addToHttpParams(httpParams: HttpParams, value: any, key?: string): HttpParams {
        if (typeof value === "object" && value instanceof Date === false) {
            httpParams = this.addToHttpParamsRecursive(httpParams, value);
        } else {
            httpParams = this.addToHttpParamsRecursive(httpParams, value, key);
        }
        return httpParams;
    }

    private addToHttpParamsRecursive(httpParams: HttpParams, value?: any, key?: string): HttpParams {
        if (value == null) {
            return httpParams;
        }

        if (typeof value === "object") {
            if (Array.isArray(value)) {
                (value as any[]).forEach( elem => httpParams = this.addToHttpParamsRecursive(httpParams, elem, key));
            } else if (value instanceof Date) {
                if (key != null) {
                    httpParams = httpParams.append(key, (value as Date).toISOString().substring(0, 10));
                } else {
                   throw Error("key may not be null if value is Date");
                }
            } else {
                Object.keys(value).forEach( k => httpParams = this.addToHttpParamsRecursive(
                    httpParams, value[k], key != null ? `${key}.${k}` : k));
            }
        } else if (key != null) {
            httpParams = httpParams.append(key, value);
        } else {
            throw Error("key may not be null if value is not object or array");
        }
        return httpParams;
    }

    /**
     * Exports data to csv
     * @param entityName The name of the entity to export
     * @param exportMode The export mode
     * @param searchModel 
     * @param reference The entity model reference
     * @param locale The locale to use
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public exportCsv(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<Blob>;
    public exportCsv(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<HttpResponse<Blob>>;
    public exportCsv(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<HttpEvent<Blob>>;
    public exportCsv(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<any> {
        if (entityName === null || entityName === undefined) {
            throw new Error('Required parameter entityName was null or undefined when calling exportCsv.');
        }
        if (exportMode === null || exportMode === undefined) {
            throw new Error('Required parameter exportMode was null or undefined when calling exportCsv.');
        }
        if (searchModel === null || searchModel === undefined) {
            throw new Error('Required parameter searchModel was null or undefined when calling exportCsv.');
        }

        let localVarQueryParameters = new HttpParams({encoder: this.encoder});
        if (reference !== undefined && reference !== null) {
          localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
            <any>reference, 'reference');
        }
        if (locale !== undefined && locale !== null) {
          localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
            <any>locale, 'locale');
        }
        if (exportMode !== undefined && exportMode !== null) {
          localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
            <any>exportMode, 'exportMode');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (Keycloak) required
        localVarCredential = this.configuration.lookupCredential('Keycloak');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Bearer ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                '*/*',
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }

        let localVarHttpContext: HttpContext | undefined = options && options.context;
        if (localVarHttpContext === undefined) {
            localVarHttpContext = new HttpContext();
        }


        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }

        let localVarPath = `/api/dynamo/export/csv/${this.configuration.encodeParam({name: "entityName", value: entityName, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}`;
        return this.httpClient.request('post', `${this.configuration.basePath}${localVarPath}`,
            {
                context: localVarHttpContext,
                body: searchModel,
                params: localVarQueryParameters,
                responseType: "blob",
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

    /**
     * Exports data to Excel
     * @param entityName The name of the entity to export
     * @param exportMode The export mode
     * @param searchModel 
     * @param reference The entity model reference
     * @param locale The locale to use
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public exportExcel(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe?: 'body', reportProgress?: boolean, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<Blob>;
    public exportExcel(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe?: 'response', reportProgress?: boolean, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<HttpResponse<Blob>>;
    public exportExcel(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe?: 'events', reportProgress?: boolean, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<HttpEvent<Blob>>;
    public exportExcel(entityName: string, exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID', searchModel: SearchModel, reference?: string, locale?: string, observe: any = 'body', reportProgress: boolean = false, options?: {httpHeaderAccept?: '*/*' | 'application/json', context?: HttpContext}): Observable<any> {
        if (entityName === null || entityName === undefined) {
            throw new Error('Required parameter entityName was null or undefined when calling exportExcel.');
        }
        if (exportMode === null || exportMode === undefined) {
            throw new Error('Required parameter exportMode was null or undefined when calling exportExcel.');
        }
        if (searchModel === null || searchModel === undefined) {
            throw new Error('Required parameter searchModel was null or undefined when calling exportExcel.');
        }

        let localVarQueryParameters = new HttpParams({encoder: this.encoder});
        if (reference !== undefined && reference !== null) {
          localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
            <any>reference, 'reference');
        }
        if (locale !== undefined && locale !== null) {
          localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
            <any>locale, 'locale');
        }
        if (exportMode !== undefined && exportMode !== null) {
          localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
            <any>exportMode, 'exportMode');
        }

        let localVarHeaders = this.defaultHeaders;

        let localVarCredential: string | undefined;
        // authentication (Keycloak) required
        localVarCredential = this.configuration.lookupCredential('Keycloak');
        if (localVarCredential) {
            localVarHeaders = localVarHeaders.set('Authorization', 'Bearer ' + localVarCredential);
        }

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
                '*/*',
                'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }

        let localVarHttpContext: HttpContext | undefined = options && options.context;
        if (localVarHttpContext === undefined) {
            localVarHttpContext = new HttpContext();
        }


        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }

        let localVarPath = `/api/dynamo/export/excel/${this.configuration.encodeParam({name: "entityName", value: entityName, in: "path", style: "simple", explode: false, dataType: "string", dataFormat: undefined})}`;
        return this.httpClient.request('post', `${this.configuration.basePath}${localVarPath}`,
            {
                context: localVarHttpContext,
                body: searchModel,
                params: localVarQueryParameters,
                responseType: "blob",
                withCredentials: this.configuration.withCredentials,
                headers: localVarHeaders,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

}
