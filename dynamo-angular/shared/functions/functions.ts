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
import { addMinutes, formatISO } from 'date-fns';

export function getSimpleLocale(locale: string) {
  let index = locale.indexOf('_');
  if (index > 0) {
    return locale.substring(0, index);
  }
  index = locale.indexOf('-');
  if (index > 0) {
    return locale.substring(0, index);
  }

  return locale;
}

/**
 * Converts a time in String representation (HH:mm) into a date
 * @param val the value to convert
 * @returns the result of the conversion
 */
export function timeToDate(val: string): Date {
  let date = new Date();
  date.setHours(parseInt(val.substring(0, 2)));
  date.setMinutes(parseInt(val.substring(3, 5)));
  return date;
}

/**
 * Adjusts a time stamp to UTC by taking the browser offset into account
 * @param object the time stamp to
 * @param isInstant whether the time stamp is an instant
 * @returns
 */
export function adjustTimestamp(object: any, isInstant: boolean): Date {
  if (!object) {
    return object;
  }

  let date: Date = new Date(object as string);
  if (isInstant) {
    let offset = date.getTimezoneOffset();
    return addMinutes(date, offset);
  }
  return date;
}

/**
 * Converts a Javascript date to a string containing the ISO representation of the date
 * @param input the input
 * @returns the result of the conversion
 */
export function dateToString(val: any): string | undefined {
  return val ? formatISO(val, { representation: 'date' }) : undefined;
}

/**
 * Converts a string representing a time stamp to a JavaScript date,
 * taking time zone offsets into account
 * @param val the value to convert
 * @param instant whether the value represents an Instant
 * @returns the result of the conversion
 */
export function timestampToDate(
  val: string,
  instant: boolean
): Date | undefined {
  if (!val) {
    return undefined;
  }

  let temp = formatISO(val, { representation: 'complete' });
  let date = new Date(temp);

  // compensate for time zone
  if (instant) {
    const d = new Date();
    let diff = d.getTimezoneOffset();
    date = addMinutes(date, diff);
  }

  return date;
}

/**
 * Converts a string to a time stamp for use in the JSON request
 * @param val the string value to convert
 * @returns the result of the conversion
 */
export function stringToTime(val: string): string | undefined {
  if (!val) {
    return undefined;
  }

  // directly entered time
  if (val.length == 5) {
    return val;
  }

  return formatISO(val, {
    representation: 'time',
  }).substring(0, 5);
}

/**
 * Converts a Javascript date to a time stamp for JSON transmission
 * @param val the value to convert
 * @param instant whether the converted value is an instant
 * @returns the result of the conversion
 */
export function dateToTimestamp(val: any, instant: boolean) {
  if (!val) {
    return undefined;
  }

  let timeStr = formatISO(val, {
    representation: 'complete',
  });

  // strip off time
  let p = timeStr.indexOf('+');
  if (p > 0) {
    return timeStr.substring(0, p) + (instant ? 'Z' : '');
  }
  return timeStr;
}

/**
 * Retrieves a nested value from an object
 * @param obj  the object
 * @param name  the name (can include multiple levels) of the property
 * @returns
 */
export function getNestedValue(obj: any, name: string): any {
  let p = name.indexOf('.');
  while (p >= 0) {
    let part = name.substring(0, p);
    obj = obj[part];
    name = name.substring(p + 1);
    p = name.indexOf('.');
  }

  obj = obj ? obj[name] : undefined;
  return obj;
}

export function getCalendarDateFormat(format: string) {
  format = format.replaceAll('yyyy', 'yy');
  format = format.replace('MM', 'mm');
  return format;
}

export function getLocale() {
  // let locale = '';
  // if (navigator.languages != undefined) {
  //   locale = getSimpleLocale(navigator.languages[0]);
  // } else {
  //   locale = getSimpleLocale(navigator.language);
  // }
  // console.log('using locale ' + locale);
  // return locale;
  return 'en';
}

export function prependUrl(input: string) {
  if (!input) {
    return input;
  }

  if (!input.startsWith('http://')) {
    return 'http://' + input;
  }
  return input;
}

export function decapitalize(input: string) {
  if (!input) {
    return input;
  }

  return input.substring(0, 1).toLowerCase() + input.substring(1);
}
