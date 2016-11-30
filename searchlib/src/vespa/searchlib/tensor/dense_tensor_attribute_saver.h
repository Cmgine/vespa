// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/searchlib/attribute/attributesaver.h>
#include <vespa/searchlib/attribute/iattributesavetarget.h>
#include "tensor_attribute.h"

namespace search {

namespace tensor {

class DenseTensorStore;

/*
 * Class for saving a tensor attribute.
 */
class DenseTensorAttributeSaver : public AttributeSaver
{
public:
    using RefCopyVector = TensorAttribute::RefCopyVector;
private:
    RefCopyVector      _refs;
    const DenseTensorStore &_tensorStore;
    using GenerationHandler = vespalib::GenerationHandler;

    virtual bool onSave(IAttributeSaveTarget &saveTarget) override;
public:
    DenseTensorAttributeSaver(GenerationHandler::Guard &&guard,
                              const IAttributeSaveTarget::Config &cfg,
                              RefCopyVector &&refs,
                              const DenseTensorStore &tensorStore);

    virtual ~DenseTensorAttributeSaver();
};

} // namespace search::tensor

} // namespace search
