import { Component, Input } from '@angular/core';
import { AttributeModelResponse } from 'dynamo/model';

@Component({
  selector: 'app-label',
  templateUrl: './label.component.html',
  styleUrls: ['./label.component.scss']
})
export class LabelComponent {

  @Input() attributeModel!: AttributeModelResponse
  @Input() locale!: string
}
