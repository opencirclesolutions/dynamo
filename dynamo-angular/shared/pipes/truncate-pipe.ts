import { Pipe, PipeTransform } from "@angular/core";

/**
 * Pipe for truncating a String field after a certain length
 */
@Pipe({
  name: 'truncate',
})
export class TruncatePipe implements PipeTransform {

  transform(input: string, maxLength?: number): string {

    if (!maxLength) {
      return input;
    }

    if (input.length > maxLength) {
      return input.substring(0, maxLength) + "..."
    }
    return input;
  }

}
