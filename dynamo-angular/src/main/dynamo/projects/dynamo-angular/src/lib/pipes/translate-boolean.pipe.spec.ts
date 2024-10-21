import { TranslateBooleanPipe } from './translate-boolean.pipe';

describe('TranslateBooleanPipe', () => {
  it('create an instance', () => {
    const pipe = new TranslateBooleanPipe();
    expect(pipe).toBeTruthy();
  });
});
