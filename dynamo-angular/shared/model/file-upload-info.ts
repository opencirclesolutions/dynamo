import { AttributeModelResponse } from "dynamo/model";

export interface FileUploadInfo {

  am: AttributeModelResponse;
  file: File;
  fileName: string;
}
