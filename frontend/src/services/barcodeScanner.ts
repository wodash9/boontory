import { BrowserMultiFormatReader } from '@zxing/browser'
import { BarcodeFormat, DecodeHintType, NotFoundException } from '@zxing/library'

export function createBarcodeScanner() {
  const hints = new Map()
  hints.set(DecodeHintType.POSSIBLE_FORMATS, [BarcodeFormat.EAN_13])
  return new BrowserMultiFormatReader(hints)
}

export async function scanIsbnFromVideo(
  videoElement: HTMLVideoElement,
  onDetected: (value: string) => void,
) {
  const scanner = createBarcodeScanner()

  const controls = await scanner.decodeFromVideoDevice(
    undefined,
    videoElement,
    (result, error) => {
      if (result?.getText()) {
        controls.stop()
        onDetected(result.getText())
        return
      }

      if (error && !(error instanceof NotFoundException)) {
        console.warn(error)
      }
    },
  )

  return () => {
    controls.stop()
  }
}
